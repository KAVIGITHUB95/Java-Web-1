function indexOnloadFunctions() {
    animationstart();
    checkSessionCart();
    loadProductData();
    animationend();
}

async function checkSessionCart() {
    try {
        const response = await fetch("CheckSessionCart");

        if (!response.ok) {
            alert("Something went wrong! Try again shortly.");
        } else {
            console.log("Session cart checked and synced (if needed).");
        }
    } catch (error) {
        console.error("Error checking session cart:", error);
        alert("An unexpected error occurred.");
    }
}

async function loadProductData() {
    const popup = new Notification();
    
    const response = await fetch("LoadHomeData");
    
    animationstart();
    
    if (response.ok) {
        const json = await response.json();

        if (json.status) {
            console.log(json);
//            loadBrands(json);
            loadNewArrivals(json);
        } else {
            alert("Something went wrong! Try again later!");
        }

    } else {
        alert("Something went wrong! Try again shortly");
    }
    animationend();
}

function loadBrands(json) {
    const product_brand_container = document.getElementById("product-brand-container");
    let product_brand_card = document.getElementById("product-brand-card");
    product_brand_container.innerHTML = "";
    let card_delay = 200;
    json.brandList.forEach(item => {
        let product_brand_card_clone = product_brand_card.cloneNode(true);
        product_brand_card_clone.querySelector("#product-brand-mini-card")
                .setAttribute("data-sal", "zoom-out");
        product_brand_card_clone.querySelector("#product-brand-mini-card")
                .setAttribute("data-sal-delay", card_delay);
        product_brand_card_clone.querySelector("#product-brand-a")
                .href = "search.html";
        product_brand_card_clone.querySelector("#product-brand-title")
                .innerHTML = item.name;
        product_brand_container.appendChild(product_brand_card_clone);
        card_delay += 100;

        sal();
    });
}


function loadNewArrivals(json) {
    console.log(json);
    const container = document.getElementById("product-container");
    container.innerHTML = "";

    json.stockList.forEach(stock => {
        const product = stock.product;
        const model = stock.product.model;
        const brand = stock.product.model.brand;
        const color = stock.color;
        const size = stock.size;

        const card = `
            <article>
                <div class="card shadow-sm rounded-4 mx-2" style="width: 100%">
                    <div class="position-relative p-3 pt-4">
                        <img src="product-images/${product.id}/image1.png"
                             class="card-img-top rounded" alt="${product.title}" style="max-width: 80rem; width: 100%;"/>
                        <div class="position-absolute top-0 end-0 mt-2 me-2 d-flex flex-column gap-2">
                            <button class="btn btn-light icon-btn shadow-sm">
                                <i class="bi bi-heart"></i>
                            </button>
                            <button class="btn btn-light icon-btn shadow-sm">
                                <i class="bi bi-arrow-clockwise"></i>
                            </button>
                        </div>
                    </div>
                    <div class="card-body pt-2">
                        <div class="mb-1">
                            <span class="text-warning">★★★★★</span>
                            <span class="text-muted">(142)</span>
                        </div>
                        <h6>
                            <a href="single-product.html?id=${stock.id}" class="text-decoration-none text-dark">
                                ${product.title} - Size ${size.value}
                            </a>
                        </h6>
                        <div class="d-flex justify-content-between align-items-center my-3">
                            <h5 class="mb-0">Rs. ${new Intl.NumberFormat("en-US", { minimumFractionDigits: 2 }).format(stock.price)}</h5>
                            <button class="btn btn-danger" onclick="addToCart(${stock.id}, 1)">
                                <i class="bi bi-cart"></i>
                            </button>
                        </div>
                        <ul class="list-unstyled small text-muted prod-down">
                            <li class="d-flex justify-content-between"><span>Color:</span><span><span class="badge" style="background-color: ${color.value};">&nbsp;</span></span></li>
                            <li class="d-flex justify-content-between"><span>Size:</span><span>${size.value}</span></li>
                            <li class="d-flex justify-content-between"><span>ID:</span><span>${stock.id}</span></li>
                        </ul>
                    </div>
                </div>
            </article>`;
        container.innerHTML += card;
    });
}

async function addToCart(stockId, qty) {
    const response = await fetch("AddToCart?stId=" + stockId + "&qty=" + qty);
    if (response.ok) {
        const json = await response.json();
        if (json.status) {
            alert(json.message);
        } else {
            alert(json.message);
            alert("Something went wrong. Try again!");
        }
    } else {
        alert(json.message);
        alert("Something went wrong. Try again!");
    }
}
