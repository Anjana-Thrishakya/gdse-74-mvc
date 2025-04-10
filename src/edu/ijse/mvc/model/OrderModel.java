/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.ijse.mvc.model;

import edu.ijse.mvc.db.DBConnection;
import edu.ijse.mvc.dto.OrderDetailDto;
import edu.ijse.mvc.dto.OrderDto;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * @author anjana
 */
public class OrderModel {
    public String placeOrder(OrderDto orderDto, ArrayList<OrderDetailDto> orderDetailDtos) throws  Exception{
        String resp = "Success";
        Connection connection = DBConnection.getInstance().getConnection();
        
        try{
            connection.setAutoCommit(false);
            String orderSQL = "INSERT INTO Orders VALUES (?,?,?)";
            PreparedStatement orderStatement = connection.prepareStatement(orderSQL);
            orderStatement.setString(1, orderDto.getOrderId());
            orderStatement.setString(2, orderDto.getOrderDate());
            orderStatement.setString(3, orderDto.getCustId());
            
            boolean isOrderSaved = orderStatement.executeUpdate() > 0;
            
            if(isOrderSaved){
                
                boolean isOrderDetailSaved = true;
                String orderDetailSQL = "INSERT INTO OrderDetail VALUES (?,?,?,?)";
                for (OrderDetailDto orderDetailDto : orderDetailDtos) {
                    PreparedStatement orderDetailStatement = connection.prepareStatement(orderDetailSQL);
                    orderDetailStatement.setString(1, orderDto.getOrderId());
                    orderDetailStatement.setString(2, orderDetailDto.getItemCode());
                    orderDetailStatement.setInt(3, orderDetailDto.getQty());
                    orderDetailStatement.setInt(4, orderDetailDto.getDiscount());
                    
                    if(!(orderDetailStatement.executeUpdate() > 0)){
                        isOrderDetailSaved = false;
                    }
                }
                
                if(isOrderDetailSaved){
                    
                    boolean isItemUpdated = true;
                    String itemUpdateSql = "UPDATE Item SET QtyOnHand = (QtyOnHand - ?) WHERE ItemCode = ?";
                    
                    for (OrderDetailDto orderDetailDto : orderDetailDtos) {
                        PreparedStatement itemStatement = connection.prepareStatement(itemUpdateSql);
                        itemStatement.setInt(1, orderDetailDto.getQty());
                        itemStatement.setString(2, orderDetailDto.getItemCode());
                        
                        if(!(itemStatement.executeUpdate() > 0)){
                            isItemUpdated = false;
                        }
                    }
                    
                    if(isItemUpdated){
                        connection.commit();
                    } else {
                        connection.rollback();
                        resp = "Item Update Error";
                    }
                    
                    
                } else {
                    connection.rollback();
                    resp = "Order Detail Save Error";
                }
                
            } else {
                connection.rollback();
                resp = "Order Save Error";
            }
            
            return resp;
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
        
        
    }
}
