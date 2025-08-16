window.addEventListener("DOMContentLoaded", async function () {
    console.log("DOM ready, calling functions...");
    animationstart();
    adminloadOrder();
    adminloadProducts();
    loadAdminDashboard();
    await loadFields();

    
    loadAllUsers();
    animationend();
});

let currentPage = 1;
const pageSize = 5; // orders per page
let ordersData = []; // store fetched orders

async function loadAdminDashboard(page = 1) {
    try {
        animationstart();
        const response = await fetch("AdminDashboardData");
        const data = await response.json();

        if (!data.status) {
            alert(data.message || "Failed to load dashboard data.");
            return;
        }

        // Store orders for pagination
        ordersData = data.orders;
        currentPage = page;

        const totalSales = ordersData.reduce((sum, order) => sum + order.total, 0);

        document.getElementById('totalSales').textContent = `Rs. ${totalSales.toFixed(2)}`;
        document.getElementById('totalOrders').textContent = data.totalOrders;
        document.getElementById('pendingDelivery').textContent = data.pendingDeliveries;
        document.getElementById('noofCustomers').textContent = data.customers;

        renderOrdersTable();
        renderPagination();

    } catch (error) {
        console.error('Error loading admin dashboard:', error);
        alert('Error loading dashboard data.');
    } finally {
        animationend();
    }
}

function renderOrdersTable() {
    const tbody = document.querySelector('table.table tbody');
    tbody.innerHTML = '';

    const start = (currentPage - 1) * pageSize;
    const paginatedOrders = ordersData.slice(start, start + pageSize);

    paginatedOrders.forEach(order => {
        let statusLabel = 'Unknown';
        let badgeClass = 'bg-secondary';
         
        console.log(order.statusId);
        
        switch (order.statusId) {
            case 1: statusLabel = 'Completed'; badgeClass = 'bg-success'; break;
            case 2: statusLabel = 'Pending'; badgeClass = 'bg-warning text-dark'; break;
            case 3: statusLabel = 'Cancelled'; badgeClass = 'bg-danger'; break;
            default: statusLabel = 'Processing'; badgeClass = 'bg-info';
        }

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>#00${order.orderId.toString().padStart(3, '0')}</td>
            <td>${order.user?.first_name || 'N/A'} ${order.user?.last_name || 'N/A'}</td>
            <td><span class="badge ${badgeClass}">${statusLabel}</span></td>
            <td>Rs. ${order.total.toFixed(2)}</td>
        `;
        tbody.appendChild(tr);
    });
}

function renderPagination() {
    const pagination = document.getElementById('ordersPagination');
    pagination.innerHTML = '';

    const totalPages = Math.ceil(ordersData.length / pageSize);

    for (let i = 1; i <= totalPages; i++) {
        const li = document.createElement('li');
        li.className = `page-item ${i === currentPage ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#">${i}</a>`;
        li.addEventListener('click', (e) => {
            e.preventDefault();
            loadAdminDashboard(i); // load selected page
        });
        pagination.appendChild(li);
    }
}

async function adminloadProducts() {
    const container = document.getElementById("products-container");
    animationstart();

    try {
        const response = await fetch("AdminLoadProducts");

        if (response.ok) {
            const json = await response.json();

            if (json.status) {
                loadProducts(json.products);
                let html = "";

                // Iterate over products array
                json.products.forEach(stock => {
                    html += `
                    <div class="card shadow-lg border-0 rounded-4 mb-4">
    <!-- Card Header -->
    <div class="card-header bg-gradient bg-primary text-white rounded-top-4 d-flex justify-content-between align-items-center">
        <h5 class="mb-0 fw-bold">Product: ${stock.product?.title || "N/A"}</h5>
        <span class="badge bg-light text-dark px-3 py-2">Stock ID: ${stock.id || "N/A"}</span>
    </div>

    <!-- Card Body -->
    <div class="card-body">
        <div class="row g-4 align-items-center">
            <!-- Product Image -->
            <div class="col-md-4 text-center">
                <img src="product-images/${stock.product.id}/image1.png" 
                     class="img-fluid rounded-3 shadow-sm border" 
                     alt="${stock.id}" 
                     style="max-width: 50%; height: auto;">
            </div>

            <!-- Product Details -->
            <div class="col-md-8">
                <ul class="list-unstyled mb-4">
                    <li class="mb-2"><strong>Price:</strong> <span class="text-success fw-bold">Rs.${stock.price?.toFixed(2) || "0.00"}</span></li>
                    <li class="mb-2"><strong>Stock Quantity:</strong> ${stock.qty || 0}</li>
                    <li class="mb-2"><strong>Colour:</strong> ${stock.color?.value || "N/A"}</li>
                    <li class="mb-2"><strong>Size:</strong> ${stock.size?.value || "N/A"}</li>
                    <li><strong>Description:</strong> <span class="text-muted">${stock.product?.description || "No description available."}</span></li>
                </ul>

                <!-- Action Buttons -->
                <div class="d-flex gap-2">
                    <button class="btn btn-warning btn-sm" onclick="updateProduct(${stock.id});">
                        <i class="bi bi-pencil-square"></i> Update
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
                    `;
                });

                container.innerHTML = html;
            } else {
                container.innerHTML = `<div class="alert alert-warning">${json.message || "No products found."}</div>`;
            }
        } else {
            container.innerHTML = `<div class="alert alert-danger">Failed to load products. Status: ${response.status}</div>`;
        }
    } catch (error) {
        container.innerHTML = `<div class="alert alert-danger">Error loading products: ${error.message}</div>`;
    } finally {
        animationend();
    }
}

async function adminloadOrder() {
    const container = document.getElementById("orders-container");
    const response = await fetch("AdminLoadOrder");
    animationstart();

    if (response.ok) {
        const json = await response.json();

        if (json.status) {
            let html = "";

            // Iterate over orders array (each order includes its items)
            json.orders.forEach(order => {
                html += `
                <div class="card mb-4">
                    <div class="card-header bg-primary text-white">
                        <strong>Order #000${order.id}</strong>
                        <span class="float-end">${new Date(order.created_at).toLocaleDateString()}</span>
                    </div>
                    <div class="card-body">
                        <h6>Shipping Address:</h6>
                        <p>
                            User ID: ${order.user.id}</br>
                            Name: ${order.user.first_name} ${order.user.last_name}</br> 
                            ${order.address?.lineOne || ""} ${order.address?.lineTwo || ""}<br>
                            ${order.address?.city?.name || ""}, ${order.address?.postalCode || ""}
                        </p>
                        <p> Status ID: ${order.order_status.value} </p>
                        <p> Order ID : ${order.id} </p>
                        <p> Total Amount : ${order.total} </p>

                        <h6>Items:</h6>
                        <div class="table-responsive">
                            <table class="table table-striped table-sm">
                                <thead>
                                    <tr>
                                        <th>Product</th>
                                        <th>Qty</th>
                                        <th>Unit Price</th>
                                        <th>Rating</th>
                                    </tr>
                                </thead>
                                <tbody>
                `;

                // Iterate over orderItems inside the order
                order.orderItems.forEach(item => {
                    html += `
                    <tr>
                        <td>${item.stock?.product?.title || "N/A"}</td>
                        <td>${item.qty}</td>
                        <td>Rs.${item.price || 'N/A'}</td>
                        <td>${item.rating || 0}</td>
                    </tr>
                    `;
                });

                html += `
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                `;
            });

            container.innerHTML = html;
        } else {
            container.innerHTML = `<div class="alert alert-warning">${json.message || "No orders found."}</div>`;
        }
        animationend();
    }
}

var modelList;
//var productList;

async function loadFields() {
    console.log("Ok");
    console.log("loadFields started");
    try {
        const response = await fetch("LoadProductData");
        console.log("Response received", response);
        console.log("Response status:", response.status);

        if (response.ok) {
            const json = await response.json();
            console.log("Response JSON:", json);
            console.log("json.status:", json.status);

            if (json.status) {
                loadBrand(json.brandList);
                loadSelect("brand", json.brandList, "name");
                modelList = json.modelList;
                console.log(json.sizeList);
                console.log(json.colorList);
                loadSelect("category", json.categoryList, "name");
                loadSelect("size", json.sizeList, "value");
                loadSelect("color", json.colorList, "value");
//                loadSelect("update-size", json.sizeList, "value");
//                loadSelect("update-color", json.colorList, "value");
            } else {
                alert("Unable to load product data! Please try again later.");
            }
        } else {
            alert("Unable to load product data! Please try again later.");
        }
    } catch (error) {
        console.error("Error loading product data:", error);
        alert("Unable to load product data! Please try again later.");
    }
}


function loadSelect(selectId, list, property) {
    const select = document.getElementById(selectId);
    select.length = 1; // keep only the default 'Select' option

    list.forEach(item => {
        const option = document.createElement("option");
        option.value = item.id;
        option.textContent = item[property];
        select.appendChild(option);
    });
}


function loadModels() {

    const brandId = document.getElementById("brand").value;

    const modelSelect = document.getElementById("model");

    modelSelect.length = 1;
    modelList.forEach(item => {

        if (item.brand.id == brandId) {

            const option = document.createElement("option");
            option.value = item.id;
            option.innerHTML = item.name;

            modelSelect.appendChild(option);
        }

    });

}

async function saveProduct() {

    const brandId = document.getElementById("brand").value;
    const modelId = document.getElementById("model").value;
    const categoryId = document.getElementById("category").value;
//    const productId = document.getElementById("product").value;
    const sizeId = document.getElementById("size").value;
    const colorId = document.getElementById("color").value;

    const title = document.getElementById("title").value;
    const description = document.getElementById("description").value;

    const price = document.getElementById("price").value;
    const qty = document.getElementById("qty").value;

    const image1 = document.getElementById("img1").files[0];

    const image2 = document.getElementById("img2").files[0];
    const image3 = document.getElementById("img3").files[0];


    const form = new FormData();
    form.append("brandId", brandId);
    form.append("modelId", modelId);
    form.append("categoryId", categoryId);
//    form.append("productId", productId);
    form.append("sizeId", sizeId);
    form.append("colorId", colorId);
    form.append("title", title);
    form.append("description", description);
    form.append("price", price);
    form.append("qty", qty);
    form.append("image1", image1);
    form.append("image2", image2);
    form.append("image3", image3);

    const response = await fetch(
            "SaveProduct",
            {
                method: "POST",
                body: form
            }

    );


    if (response.ok) {

        console.log(form);
        const json = await response.json();

        if (json.status) { //true -> success
            alert(json.message);
            document.getElementById("brand").value = "0";
            document.getElementById("model").value = "0";
            document.getElementById("category").value = "0";
//            document.getElementById("product").value = "0";
            document.getElementById("size").value = "0";
            document.getElementById("color").value = "0";
            document.getElementById("title").value = "";
            document.getElementById("description").value = "";
            document.getElementById("price").value = "0.00";
            document.getElementById("qty").value = "1";
            document.getElementById("img1").value = "";
            document.getElementById("img2").value = "";
            document.getElementById("img3").value = "";
        } else { // false -> error
            alert(json.message || "Failed to save product1");

        }
    } else {
        alert(json.message || "Failed to save product2");
    }

}

async function addData(type) {
    event.preventDefault();
    const value = document.getElementById(type).value.trim();

    if (!value) {
        alert("Please enter a value for " + type);
        return;
    }

    const response = await fetch("AddDataServlet", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `type=${encodeURIComponent(type)}&value=${encodeURIComponent(value)}`
    });

    const result = await response.json();

    if (result.status) {
        alert(type.charAt(0).toUpperCase() + type.slice(1) + " added successfully!");
        document.getElementById(type).value = "";
    } else {
        alert("Error: " + (result.message || "Something went wrong"));
    }
}

function addModel() {
    const brandId = document.getElementById("modelBrand").value;
    const modelName = document.getElementById("addModel").value;

    if (brandId === "0") {
        alert("Please select a brand before adding a model.");
        return;
    }
    if (!modelName) {
        alert("Please enter a model name.");
        return;
    }

    const params = new URLSearchParams();
    params.append("type", "model");
    params.append("brandId", brandId);
    params.append("value", modelName);

    fetch("AddDataServlet", {
        method: "POST",
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
        body: params.toString()
    })
            .then(res => res.json())
            .then(data => {
                if (data.status) {
                    alert("Model added successfully!");
                    document.getElementById("model").value = "";
                } else {
                    alert(data.message || "Failed to add model.");
                }
            })
            .catch(err => console.error(err));
}

function loadBrand(brandList) {

    const brandId = document.getElementById("modelBrand").value;

    const modelSelect = document.getElementById("model");

    brandList.forEach(item => {

        const option = document.createElement("option");
        option.value = item.id;
        option.innerHTML = item.name;

        modelSelect.appendChild(option);

    });

}


async function loadAllUsers() {
    try {
        const response = await fetch('LoadAllUsers');

        if (response.ok) {
            const json = await response.json();

            if (json.status) {
                renderUsers(json.userList);

            } else {
                alert('Failed to load users');
            }
        } else {
            throw new Error('Network response was not ok');
        }

    } catch (error) {
        console.error('Error loading users:', error);
        alert('Error loading user data.');
    }
}

function renderUsers(userList) {
    const tbody = document.getElementById('userTableBody');
    let rows = '';

    userList.forEach(user => {
        // Create badges based on verification and status
        const verificationBadge = user.verification === 'Verified'
                ? '<span class="badge bg-success">Verified</span>'
                : '<span class="badge bg-warning text-dark">Pending</span>';

        const statusBadge = user.userstatus.value === 'Active'
                ? '<span class="badge bg-success">Active</span>'
                : '<span class="badge bg-danger">Inactive</span>';

        rows += `
        <tr>
          <td>#${user.id || 'N/A'}</td>
          <td>${user?.first_name || 'N/A'}</td>
          <td>${user?.last_name || 'N/A'}</td>
          <td>${user.email || 'N/A'}</td>
          <td>${user.mobile || 'N/A'}</td>
          <td>${verificationBadge || 'N/A'}</td>
          <td>${user.gender.name || 'N/A'}</td>
          <td>${user.created_at || 'N/A'}</td>
          <td>${statusBadge || 'N/A'}</td>
        </tr>
      `;
    });

    tbody.innerHTML = rows;
}

function loadProducts(productList) {
    const tbody = document.getElementById("productTableBody");
    tbody.innerHTML = ""; // clear existing rows

    productList.forEach(stock => {
        const row = `
                    <tr>
                        <td>${stock.product?.id || "N/A"}</td>
                        <td>${stock.product?.title || "N/A"}</td>
                        <td>$${stock.price?.toFixed(2) || "0.00"}</td>
                        <td>${stock.qty || 0}</td>
                        <td>${stock.color?.value || "N/A"}</td>
                        <td>${stock.size?.value || "N/A"}</td>
                        <td>${stock.product?.description || "No description"}</td>
                    </tr>
                `;
        tbody.insertAdjacentHTML("beforeend", row);
    });

}

document.getElementById('printBtnproduct').addEventListener('click', () => {
    const productTable = document.getElementById('productTable'); // Specific product table ID
    const newWindow = window.open('', '', 'width=900,height=600');

    newWindow.document.write('<html><head><title>Product List</title>');
    newWindow.document.write('<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">');
    newWindow.document.write('<style>h2 { text-align:center; margin-bottom: 20px; }</style>');
    newWindow.document.write('</head><body>');
    newWindow.document.write('<h2>Product List</h2>');
    newWindow.document.write(productTable.outerHTML);
    newWindow.document.write('</body></html>');

    newWindow.document.close();
    newWindow.focus();
    newWindow.print();
    newWindow.close();
});

async function updateProduct(stockId) {
    try {
        // Load dropdowns first
        await loadDataModal();

        // Fetch product data
        const response = await fetch("GetSavedData?stockId=" + stockId);
        if (!response.ok)
            throw new Error(`Network error: ${response.status} ${response.statusText}`);

        const json = await response.json();
        if (!json.status)
            throw new Error(json.message || 'Failed to load product data');

        let productData = json.stock;

        // Fill form fields
        document.getElementById('update-stock-id').value = productData.id;
        document.getElementById('update-status').value = productData.status.id;
        document.getElementById('update-product-id').value = productData.product.id;
        document.getElementById('update-title').value = productData.product.title;
        document.getElementById('update-price').value = productData.price;
        document.getElementById('update-qty').value = productData.qty;
        document.getElementById('update-color').value = productData.color.id; // use ID
        document.getElementById('update-size').value = productData.size.id;   // use ID
        document.getElementById('update-description').value = productData.product.description;
        // Assuming productData has image URLs or relative paths
        document.getElementById('update-img1-preview').src = "product-images\\" + productData.product.id + "\\image1.png" || 'placeholder.png';
        document.getElementById('update-img2-preview').src = "product-images\\" + productData.product.id + "\\image2.png" || 'placeholder.png';
        document.getElementById('update-img3-preview').src = "product-images\\" + productData.product.id + "\\image3.png" || 'placeholder.png';

        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('updateProductModal'));
        modal.show();

    } catch (error) {
        console.error('Error updating product:', error);
        alert('Unable to load product data. Please try again later.');
    }
}

async function loadDataModal() {
    try {
        const response = await fetch("LoadProductData");
        if (!response.ok)
            throw new Error("Unable to load product data");

        const json = await response.json();
        if (!json.status)
            throw new Error("Unable to load product data");

        loadSelectU("update-size", json.sizeList, "value");
        loadSelectU("update-color", json.colorList, "value");
        loadSelectU("update-status", json.statusList, "value");

    } catch (error) {
        console.error("Error loading product data:", error);
        alert("Unable to load product data! Please try again later.");
    }
}

function loadSelectU(selectId, list, property) {
    const select = document.getElementById(selectId);
    if (!select)
        return;

    select.innerHTML = '<option value="0">Select</option>';
    list.forEach(item => {
        const option = document.createElement("option");
        option.value = item.id;
        option.textContent = item[property];
        select.appendChild(option);
    });
}

async function updateStock() {
    
    const stockId = document.getElementById('update-stock-id').value;
    const productId = document.getElementById('update-product-id').value;
    const statusId = document.getElementById('update-status').value;
    const sizeId = document.getElementById("update-size").value;
    const colorId = document.getElementById("update-color").value;

    const title = document.getElementById("update-title").value;
    const description = document.getElementById("update-description").value;

    const price = document.getElementById("update-price").value;
    const qty = document.getElementById("update-qty").value;

    const image1 = document.getElementById("update-img1").files[0];

    const image2 = document.getElementById("update-img2").files[0];
    const image3 = document.getElementById("update-img3").files[0];


    const form = new FormData();
    form.append("stockId", stockId);
    form.append("productId", productId);
    form.append("statusId", statusId);
    form.append("sizeId", sizeId);
    form.append("colorId", colorId);
    form.append("title", title);
    form.append("description", description);
    form.append("price", price);
    form.append("qty", qty);
    form.append("image1", image1);
    form.append("image2", image2);
    form.append("image3", image3);

    const response = await fetch(
            "GetSavedData",
            {
                method: "POST",
                body: form
            }

    );


    if (response.ok) {

        console.log(form);
        const json = await response.json();

        if (json.status) { //true -> success
            alert(json.message);
            document.getElementById('update-stock-id').value = "0";
            document.getElementById('update-product-id').value = "";
            document.getElementById('update-status').value = "0";
            document.getElementById("update-size").value = "0";
            document.getElementById("update-color").value = "0";
            document.getElementById("update-title").value = "";
            document.getElementById("update-description").value = "";
            document.getElementById("update-price").value = "0.00";
            document.getElementById("update-qty").value = "1";
            document.getElementById("update-img1").value = "";
            document.getElementById("update-img2").value = "";
            document.getElementById("update-img3").value = "";
            adminloadProducts();
        } else { // false -> error
            alert(json.message || "Failed to save product1");

        }
    } else {
        alert(json.message || "Failed to save product2");
    }

}