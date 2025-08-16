window.addEventListener("load", function () {
    loadCart();
});

async function loadCart() {
    try {
        const response = await fetch("LoadCart");

        if (!response.ok) {
            alert("Cart Items loading failed...");
            return;
        }

        const json = await response.json();
        console.log(json);

        if (json.status) {
            const tbody = document.getElementById("cart-body");
            const totalField = document.getElementById("cart-total");
            let total = 0;
            tbody.innerHTML = "";

            json.cartItems.forEach(cart => {
                const subtotal = cart.qty * cart.stock.price;
                total += subtotal;

                tbody.innerHTML += `
                    <tr>
                        <td>
                            <img src="product-images/${cart.stock.product.id}/image1.png" 
                                 alt="${cart.stock.product.title}" width="50" class="me-2">
                        </td>
                        <td>${cart.stock.product.title}</td>
                        <td>${cart.stock.color.value}</td>
                        <td>${cart.stock.size.value}</td>
                        <td>Rs ${new Intl.NumberFormat("en-US", {
                    minimumFractionDigits: 2
                }).format(cart.stock.price)}</td>
                        <td>
                            <input type="number" class="form-control w-75" value="${cart.qty}" min="1" onchange="updateCartQty(${cart.stock.id}, this.value)">
                        </td>
                        <td>Rs ${subtotal.toFixed(2)}</td>
                        <td>
                            <button class="btn btn-sm btn-danger" onclick="removeCartItem(${cart.stock.id})">Remove</button>
                        </td>
                    </tr>`;
            });

            totalField.textContent = `Rs ${total.toFixed(2)}`;
        }
    } catch (error) {
        console.error("Error loading cart:", error);
        alert("Something went wrong while loading your cart.");
    }
}

async function updateCartQty(stockId, newQty) {
    if (newQty < 1) {
        alert("Quantity must be at least 1.");
        return;
    }

    try {
        const response = await fetch(`UpdateCartQty?stockId=${stockId}&qty=${newQty}`);
        const json = await response.json();

        if (json.status) {
            // Success: optionally reload cart or update subtotal only
            loadCart(); // Reloads the cart to reflect changes
        } else {
            loadCart();
            alert(json.message || "Failed to update quantity.");
        }
    } catch (error) {
        console.error("Error updating cart quantity:", error);
        alert("An error occurred while updating the cart.");
    }

}

async function removeCartItem(stockId) {
    try {
        const response = await fetch(`RemoveCartItem?stockId=${stockId}`);

        if (response.ok) {
            const json = await response.json();
            console.log(json);

            alert(json.message);

            if (json.status) {
                loadCart(); // Reload cart items
            }
        } else {
            alert("Failed to remove item from cart.");
        }
    } catch (error) {
        console.error("Error:", error);
        alert("An error occurred while removing the item.");
    }
}