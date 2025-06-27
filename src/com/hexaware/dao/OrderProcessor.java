package com.hexaware.dao;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import com.hexaware.util.DBConnUtil;
import com.hexaware.entity.*;
import com.hexaware.exception.*;


public class OrderProcessor implements IOrderManagementRepository {

    @Override
    public void createUser(User user) throws Exception {
    	
        String checkQuery = "select * from User where userid = ?";
        String insertQuery = "insert into User (userid, username, password, role) values (?, ?, ?, ?)";

        try (Connection conn = DBConnUtil.getConnection("db.properties");
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            checkStmt.setInt(1, user.getUserId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("User already exists with ID: " + user.getUserId());
            } else {
                insertStmt.setInt(1, user.getUserId());
                insertStmt.setString(2, user.getUsername());
                insertStmt.setString(3, user.getPassword());
                insertStmt.setString(4, user.getRole().toLowerCase());

                int rows = insertStmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("User created successfully.");
                }
            }

        } catch (SQLException e) {
            throw new Exception("Error creating user: " + e.getMessage());
        }
    }


    @Override
    public void createProduct(User user, Product product) throws Exception {
        if (!"admin".equalsIgnoreCase(user.getRole())) {
            throw new Exception("Only admin users can create products.");
        }

        String productInsert = "insert into Product (productid, productname, description, price, quantityinstock, type) values (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnUtil.getConnection("db.properties");
             PreparedStatement prodStmt = conn.prepareStatement(productInsert)) {

           
            prodStmt.setInt(1, product.getProductId());
            prodStmt.setString(2, product.getProductName());
            prodStmt.setString(3, product.getDescription());
            prodStmt.setDouble(4, product.getPrice());
            prodStmt.setInt(5, product.getQuantityInStock());
            prodStmt.setString(6, product.getType().toLowerCase());

            int row = prodStmt.executeUpdate();
            if (row > 0) {
                System.out.println("Product inserted in 'product' table.");

                if (product instanceof Electronics) {
                    String elecInsert = "insert into Electronics (productid, brand, warrantyperiod) values (?, ?, ?)";
                    try (PreparedStatement elecStmt = conn.prepareStatement(elecInsert)) {
                        Electronics e = (Electronics) product;
                        elecStmt.setInt(1, e.getProductId());
                        elecStmt.setString(2, e.getBrand());
                        elecStmt.setInt(3, e.getWarrantyPeriod());
                        elecStmt.executeUpdate();
                        System.out.println("Product inserted in 'electronics' table.");
                    }
                } else if (product instanceof Clothing) {
                    String clothInsert = "insert into Clothing (productid, size, color) values (?, ?, ?)";
                    try (PreparedStatement clothStmt = conn.prepareStatement(clothInsert)) {
                        Clothing c = (Clothing) product;
                        clothStmt.setInt(1, c.getProductId());
                        clothStmt.setString(2, c.getSize());
                        clothStmt.setString(3, c.getColor());
                        clothStmt.executeUpdate();
                        System.out.println("Product inserted in 'clothing' table.");
                    }
                }
            }

        } catch (SQLException e) {
            throw new Exception("Error while creating product: " + e.getMessage());
        }
    }

    @Override
    public void createOrder(User user, List<Product> productList) throws Exception {
        try (Connection conn = DBConnUtil.getConnection("db.properties")) {
            conn.setAutoCommit(false); 

           
            String userCheck = "select * from User where userid = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userCheck)) {
                userStmt.setInt(1, user.getUserId());
                ResultSet rs = userStmt.executeQuery();

                if (!rs.next()) {
                    
                    String userInsert = "insert into User (userid, username, password, role) values (?, ?, ?, ?)";
                    try (PreparedStatement insertUser = conn.prepareStatement(userInsert)) {
                        insertUser.setInt(1, user.getUserId());
                        insertUser.setString(2, user.getUsername());
                        insertUser.setString(3, user.getPassword());
                        insertUser.setString(4, user.getRole().toLowerCase());
                        insertUser.executeUpdate();
                        System.out.println("New user created during order process.");
                    }
                }
            }

            
            String orderInsert = "insert into Orders (userid) values (?)";
            int generatedOrderId = -1;

            try (PreparedStatement orderStmt = conn.prepareStatement(orderInsert, PreparedStatement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, user.getUserId());
                orderStmt.executeUpdate();

                ResultSet generatedKeys = orderStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    generatedOrderId = generatedKeys.getInt(1);
                    System.out.println("Order placed with ID: " + generatedOrderId);
                } else {
                    conn.rollback();
                    throw new Exception("Order ID generation failed.");
                }
            }

           
            String orderProductInsert = "insert into OrderProduct (orderid, productid, quantity) values (?, ?, ?)";
            try (PreparedStatement opStmt = conn.prepareStatement(orderProductInsert)) {
                for (Product p : productList) {
                    opStmt.setInt(1, generatedOrderId);
                    opStmt.setInt(2, p.getProductId());
                    opStmt.setInt(3, 1); 
                    opStmt.addBatch();
                }
                opStmt.executeBatch();
            }

            conn.commit(); 

        } catch (SQLException e) {
            throw new Exception("Error while creating order: " + e.getMessage());
        }
    }



    @Override
    public void cancelOrder(int userId, int orderId) throws UserNotFoundException, OrderNotFoundException, Exception {
        try (Connection conn = DBConnUtil.getConnection("db.properties")) {

           
            String userQuery = "select * from User where userid = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userQuery)) {
                userStmt.setInt(1, userId);
                ResultSet userRs = userStmt.executeQuery();
                if (!userRs.next()) {
                    throw new UserNotFoundException("User with ID " + userId + " not found.");
                }
            }

           
            String orderQuery = "select * from Orders where orderid = ? and userid = ?";
            try (PreparedStatement orderStmt = conn.prepareStatement(orderQuery)) {
                orderStmt.setInt(1, orderId);
                orderStmt.setInt(2, userId);
                ResultSet orderRs = orderStmt.executeQuery();
                if (!orderRs.next()) {
                    throw new OrderNotFoundException("Order with ID " + orderId + " not found for user ID " + userId);
                }
            }

            
            conn.setAutoCommit(false);

         
            String deleteOrderProduct = "delete from OrderProduct where orderid = ?";
            try (PreparedStatement delOP = conn.prepareStatement(deleteOrderProduct)) {
                delOP.setInt(1, orderId);
                delOP.executeUpdate();
            }

            
            String deleteOrder = "delete from Orders where orderid = ?";
            try (PreparedStatement delOrder = conn.prepareStatement(deleteOrder)) {
                delOrder.setInt(1, orderId);
                delOrder.executeUpdate();
            }

            conn.commit();
            System.out.println("Order " + orderId + " for user " + userId + " cancelled successfully.");

        } catch (SQLException e) {
            throw new Exception("Error while cancelling order: " + e.getMessage());
        }
    }


    @Override
    public List<Product> getAllProducts() throws Exception {
        List<Product> productList = new ArrayList<>();

        String query = "select * from Product";

        try (Connection conn = DBConnUtil.getConnection("db.properties");
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("productid");
                String name = rs.getString("productname");
                String desc = rs.getString("description");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantityinstock");
                String type = rs.getString("type");

                if (type.equalsIgnoreCase("electronics")) {
                    // Fetch electronics-specific details
                    Product e = getElectronicsDetails(conn, id, name, desc, price, quantity);
                    productList.add(e);
                } else if (type.equalsIgnoreCase("clothing")) {
                    Product c = getClothingDetails(conn, id, name, desc, price, quantity);
                    productList.add(c);
                }
            }

        } catch (SQLException e) {
            throw new Exception("Error fetching products: " + e.getMessage());
        }

        return productList;
    }
    
    private Electronics getElectronicsDetails(Connection conn, int id, String name, String desc, double price, int quantity) throws SQLException {
        String query = "select * from Electronics where productid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String brand = rs.getString("brand");
                int warranty = rs.getInt("warrantyperiod");
                return new Electronics(id, name, desc, price, quantity, "electronics", brand, warranty);
            }
        }
        return null;
    }
    
    private Clothing getClothingDetails(Connection conn, int id, String name, String desc, double price, int quantity) throws SQLException {
        String query = "select * from Clothing where productid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String size = rs.getString("size");
                String color = rs.getString("color");
                return new Clothing(id, name, desc, price, quantity, "clothing", size, color);
            }
        }
        return null;
    }


    @Override
    public List<Order> getOrderByUser(User user) throws UserNotFoundException, Exception {
        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBConnUtil.getConnection("db.properties")) {

           
            String userQuery = "select * from User where userid = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userQuery)) {
                userStmt.setInt(1, user.getUserId());
                ResultSet userRs = userStmt.executeQuery();
                if (!userRs.next()) {
                    throw new UserNotFoundException("User with ID " + user.getUserId() + " not found.");
                }
            }

         
            String orderQuery = "select * from orders where userid = ?";
            try (PreparedStatement orderStmt = conn.prepareStatement(orderQuery)) {
                orderStmt.setInt(1, user.getUserId());
                ResultSet rs = orderStmt.executeQuery();

                while (rs.next()) {
                    int orderId = rs.getInt("orderid");
                    int userId = rs.getInt("userid");
                    java.sql.Timestamp orderDate = rs.getTimestamp("orderdate");

                    Order order = new Order(orderId, userId, orderDate);
                    orders.add(order);
                }
            }

        } catch (SQLException e) {
            throw new Exception("Error while fetching orders for user: " + e.getMessage());
        }

        return orders;
    }

}
