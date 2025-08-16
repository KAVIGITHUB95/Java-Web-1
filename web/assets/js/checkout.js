window.addEventListener("load", function () {
    loadCheckOutData();
});

// Payment completed — can be a success or failure depending on server validation
payhere.onCompleted = function (orderId) {
    console.log("Payment completed. OrderID: " + orderId);
    alert("Payment completed. OrderID: " + orderId);

    window.location = "payment-confirmation.html?orderId=" + orderId;
};

// Payment window closed by user
payhere.onDismissed = async function () {
    console.warn("Payment dismissed by user.");
    alert("Payment was cancelled. Order ID: " + orderId);
    cancelOrder(orderId);

};

// Payment error occurred
payhere.onError = function (error) {
    console.error("Payment Error:", error);
    alert("❌ Payment error: " + error);
    cancelOrder(orderId);
};

async function loadCheckOutData() {
    const response = await fetch("LoadCheckOutData");
    if (response.ok) {
        const json = await response.json();
        if (json.status) {

            console.log(json);

            const userAddress = json.userAddress;
            const cityList = json.cityList;
            const cartItems = json.cartList;

            const deliveryType = json.deliveryType;

            const city_select = document.getElementById("city");

            cityList.forEach(city => {
                let option = document.createElement("option");

                option.value = city.id;

                option.innerHTML = city.name;
                city_select.appendChild(option);

            });

            const current_address_checkbox = document.getElementById("checkbox1");
            current_address_checkbox.addEventListener("change", function () {


                let firstName = document.getElementById("firstName");
                let lastName = document.getElementById("lastName");
                let email = document.getElementById("email");

                let mobile = document.getElementById("mobile");
                let lineOne = document.getElementById("lineOne");
                let lineTwo = document.getElementById("lineTwo");
                let postalCode = document.getElementById("postalCode");


                if (current_address_checkbox.checked) {

                    firstName.value = userAddress.user.first_name;
                    lastName.value = userAddress.user.last_name;
                    email.value = userAddress.user.email;
                    mobile.value = userAddress.user.mobile;
                    city_select.value = userAddress.city.id;
                    city_select.disabled = true;
                    city_select.dispatchEvent(new Event("change"));
                    lineOne.value = userAddress.lineOne;
                    lineTwo.value = userAddress.lineTwo;
                    postalCode.value = userAddress.postalCode;

                } else {

                    firstName.value = "";
                    lastName.value = "";
                    email.value = "";
                    mobile.value = "";
                    city_select.value = 0;
                    city_select.disabled = false;

                    city_select.dispatchEvent(new Event("change"));
                    lineOne.value = "";
                    lineTwo.value = "";
                    postalCode.value = "";

                }
            });

            // cart-details
            console.log("Ok");
            let checkout_tbody = document.getElementById("checkout-tbody");
            let checkout_item = document.getElementById("checkout-item");
            let checkout_subtotal = document.getElementById("checkout-subtotal");
            let checkout_delivery = document.getElementById("checkout-delivery");
            let checkout_total = document.getElementById("checkout-total");

            checkout_tbody.innerHTML = "";

            let total = 0;
            let item_count = 0;
            cartItems.forEach(cart => {
                let item_clone = checkout_item.cloneNode(true);
                item_clone.querySelector("#checkout-product-title").innerHTML = cart.stock.product.title + "  Size : " + cart.stock.size.value;
                item_clone.querySelector("#checkout-product-qty").innerHTML = cart.qty;
                item_count += cart.qty;

                let item_sub_total = Number(cart.qty) * Number(cart.stock.price);

                item_clone.querySelector("#checkout-product-price").innerHTML = new Intl.NumberFormat(
                        "en-US",
                        {minimumFractionDigits: 2})
                        .format(item_sub_total);
                checkout_tbody.appendChild(item_clone);
                total += item_sub_total;

            });

            checkout_subtotal.querySelector("#checkout-product-total-amount").innerHTML = new Intl.NumberFormat(
                    "en-US",
                    {minimumFractionDigits: 2})
                    .format(total);
            checkout_tbody.appendChild(checkout_subtotal);

            let shipping_charges = 0;
            console.log(deliveryType);
            city_select.addEventListener("change", (e) => {
                let cityName = city_select[city_select.selectedIndex].innerHTML;
                if (cityName === "Colombo") {

                    shipping_charges = item_count * deliveryType[0].price;

                } else {
                    //out of colombo
                    shipping_charges = item_count * deliveryType[1].price;
                }

                checkout_delivery.querySelector("#checkout-delivery-charges").innerHTML = new Intl.NumberFormat(
                        "en-US",
                        {minimumFractionDigits: 2})
                        .format(shipping_charges);
                checkout_tbody.appendChild(checkout_delivery);

                checkout_total.querySelector("#checkout-order-total-amount").innerHTML = new Intl.NumberFormat(
                        "en-US",
                        {minimumFractionDigits: 2})
                        .format(shipping_charges + total);
                checkout_tbody.appendChild(checkout_total);
            });

        } else {

            if (json.message === "empty-cart") {
                alert("Empty cart. Please add some product");
//                window.location = "index.html";

            } else {

                alert(json.message);
            }

        }
    } else {

        if (response.status === 401) {
            window.location = "signin.html";
        }
    }
}

function formatCurrency(amount) {
    return new Intl.NumberFormat("en-US", {minimumFractionDigits: 2}).format(amount);
}



let orderId = "";
let payHerelist = "";
async function checkOut() {
    let checkbox1 = document.getElementById("checkbox1");

    let firstName = document.getElementById("firstName");
    let lastName = document.getElementById("lastName");
    let email = document.getElementById("email");
    let city_select = document.getElementById("city");
    let mobile = document.getElementById("mobile");
    let lineOne = document.getElementById("lineOne");
    let lineTwo = document.getElementById("lineTwo");
    let postalCode = document.getElementById("postalCode");

    let data = {
        isCurrentAddress: checkbox1.checked,
        firstName: firstName.value,
        lastName: lastName.value,
        email: email.value,
        citySelect: city_select.value,
        lineOne: lineOne.value,
        lineTwo: lineTwo.value,
        postalCode: postalCode.value,
        mobile: mobile.value
    };
    let dataJSON = JSON.stringify(data);


    const response = await fetch("CheckOut", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: dataJSON
    });


    if (response.ok) {
        const json = await response.json();
        if (json.status) {
            console.log(json);

            //PayHere Process
            payHerelist = json.payhereJSON;
            console.log(json.payhereJSON);
            payhere.startPayment(json.payhereJSON);
            orderId = json.payhereJSON.order_id;
            console.log(json.payhereJSON.order_id);
        } else {
            cancelOrder(orderId);
            alert(json.message || "Error: Something went wrong!");
        }
    } else {
        cancelOrder(orderId);
        alert(json.message || "Error: Something went wrong!");

    }
}


async function cancelOrder(orderId) {

    const response = await fetch("CancelOrder", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({orderId: orderId})
    });
    if (response.ok) {
        const json = await response.json();
        if (json.status) {

            console.log("Order cancelled and stock rolled back.");

            alert("Order cancelled!");
            window.location = "index.html";
        } else {
            console.warn("Failed to cancel order.");
        }

    } else {
        console.warn("Failed to cancel order.");
    }
}

async function paymentConfirmation() {
//    try {
    const searchParams = new URLSearchParams(window.location.search);
    if (searchParams.has("orderId")) {

        const orderId = searchParams.get("orderId");
        console.log(orderId);
        const response = await fetch("PaymentConfirmation", {
            method: "POST", // or "GET" depending on your servlet design
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({orderId: orderId})
        });

        if (response.ok) {
            const json = await response.json();

            if (json.status) {
                console.log(json);
                alert(json);
                viewOrderInvoice(json);
                console.log("Payment verified:", json.message);
                alert("Payment Verified Successfully!");
            } else {
                console.warn("Payment verification failed:", json.message);
                alert(json.message - "paymentConfirmation" || "Payment verification failed.");
            }
        } else {
//        throw new Error("Network response was not ok");
            console.log(json);
        }


    }
//    } catch (error) {
//        console.error("Error verifying payment:", error);
//        alert("An error occurred while verifying payment.");
//    }
}


function viewOrderInvoice(json) {

    const order = json.orderList;
    const items = json.orderItemsList;

    // Build invoice HTML
    let html = `
                <div class="success-icon mb-3">✔</div>
                <h2>Payment Successful!</h2>
                <p>Thank you for your purchase. Your payment has been received.</p>
                <hr>
            <h2>Invoice #${order.orderId}</h2>
            <p><strong>Date:</strong> ${order.created_at}</p>
            <p><strong>Customer:</strong> ${order.firstName} ${order.lastName}</p>
            <p><strong>Address:</strong> ${order.lineOne || ''} ${order.lineTwo || ''}, 
                ${order.city || ''} - ${order.postalCode || ''}</p>
            <p><strong>Delivery Type:</strong> ${order.deliveryType || 'N/A'}</p>
            <p><strong>Payment Status:</strong> ${order.orderStatus}</p>
            
            <table border="1" cellpadding="5" cellspacing="0" style="border-collapse: collapse; width:100%;">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Product</th>
                        <th>Qty</th>
                        <th>Price</th>
                        <th>Subtotal</th>
                    </tr>
                </thead>
                <tbody>
        `;

    items.forEach((item, index) => {
        const subtotal = (item.qty * item.price).toFixed(2);
        html += `
                <tr>
                    <td>${index + 1}</td>
                    <td>${item.stock.product.title || 'Unknown'}</td>
                    <td>${item.qty}</td>
                    <td>${item.price.toFixed(2)}</td>
                    <td>${subtotal}</td>
                </tr>
            `;
    });

    html += `
                </tbody>
            </table>
            <h3 style="text-align:right;">Total: ${order.total.toFixed(2)}</h3>
    
    <a href="index.html" class="btn btn-primary">Continue Shopping</a>
                <button class="btn btn-secondary" onclick="window.print()">Print Invoice</button>
        `;

    // Inject into container
    document.getElementById('invoiceContainer').innerHTML = html;

}