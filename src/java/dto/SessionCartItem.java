
package dto;

import java.io.Serializable;

public class SessionCartItem implements Serializable {
    private int stockId;
    private int qty;

    public SessionCartItem(int stockId, int qty) {
        this.stockId = stockId;
        this.qty = qty;
    }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
}
