package hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @ManyToOne
    @JoinColumn(name = "orders_id")
    private Orders orders;

    @Column(name = "qty", nullable = false)
    private int qty;

//    @ManyToOne
//    @JoinColumn(name = "order_status_id")
//    private OrderStatus order_status;

//    @ManyToOne
//    @JoinColumn(name = "delivery_type_id")
//    private DeliveryType delivery_type;

    @Column(name = "rating", length = 10, nullable = false)
    private String rating;

    @Column(name = "price", nullable = false)
    private double price;
    
    
    @Column(name = "delivery_fee", nullable = false)
    private double delivery_fee;

    public OrderItem() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

//    public OrderStatus getOrder_status() {
//        return order_status;
//    }
//
//    public void setOrder_status(OrderStatus order_status) {
//        this.order_status = order_status;
//    }

//    public DeliveryType getDelivery_type() {
//        return delivery_type;
//    }
//
//    public void setDelivery_type(DeliveryType delivery_type) {
//        this.delivery_type = delivery_type;
//    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDelivery_fee() {
        return delivery_fee;
    }

    public void setDelivery_fee(double delivery_fee) {
        this.delivery_fee = delivery_fee;
    }



}
