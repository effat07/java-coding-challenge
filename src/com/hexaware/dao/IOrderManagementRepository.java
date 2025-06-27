package com.hexaware.dao;

import java.util.List;
import com.hexaware.entity.*;
import com.hexaware.exception.*;

public interface IOrderManagementRepository {

    
    void createUser(User user) throws Exception;

   
    void createProduct(User user, Product product) throws Exception;

    
    void createOrder(User user, List<Product> productList) throws Exception;

   
    void cancelOrder(int userId, int orderId) throws UserNotFoundException, OrderNotFoundException, Exception;

    
    List<Product> getAllProducts() throws Exception;

   
    List<Order> getOrderByUser(User user) throws UserNotFoundException, Exception;
}

