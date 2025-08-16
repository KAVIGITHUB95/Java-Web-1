const mainImage = document.getElementById("mainImage");
const thumbnailContainer = document.getElementById("thumbnailContainer");

function updateMainImage(src, clickedThumbnail) {
    mainImage.src = src;
    document.querySelectorAll(".thumbnail").forEach(thumb => {
        thumb.classList.remove("active");
    });
    clickedThumbnail.classList.add("active");
}

function loadThumbnails(imageUrls) {

    // Load thumbnails
    imageUrls.forEach((url, index) => {
        const img = document.createElement("img");
        img.src = url;
        img.classList.add("thumbnail");
        if (index === 0)
            img.classList.add("active");
        img.addEventListener("click", () => updateMainImage(url, img));
        thumbnailContainer.appendChild(img);
    });
}

// Set initial image
//mainImage.src = imageUrls[0];

window.addEventListener("load", function () {
    loadData();
});

async function loadData() {
    const searchParams = new URLSearchParams(window.location.search);
    if (searchParams.has("id")) {

        const stockId = searchParams.get("id");
        console.log(stockId);

        try {
            animationstart();
            const response = await fetch("LoadSingleProduct?id=" + stockId);

            if (response.ok) {
                const json = await response.json();
                if (json.status) {
                    const imageUrls = [
                        "product-images\\" + json.stock.product.id + "\\image1.png",
                        "product-images\\" + json.stock.product.id + "\\image2.png",
                        "product-images\\" + json.stock.product.id + "\\image3.png"
                    ];

                    // Load thumbnails
                    loadThumbnails(imageUrls);

                    // Set initial image
                    mainImage.src = imageUrls[0];

                    document.getElementById("productTitle").innerHTML = json.stock.product.title;
//                    document.getElementById("published-on").innerHTML = json.stock.product.created_at;
                    document.getElementById("productPrice").innerHTML = "Rs." + new Intl.NumberFormat(
                            "en-US",
                            {minumFractionDigits: 2})
                            .format(json.stock.price);
                    console.log(json.stock.price);
                    document.getElementById("brand-name").innerHTML = json.stock.product.model.brand.name;
                    document.getElementById("size").innerHTML = json.stock.size.value;
                    document.getElementById("color").innerHTML = json.stock.color.value;

                    //product-description
                    document.getElementById("productDescription").innerHTML = json.stock.product.description;


                    //add-to-cart-main-button
                    const addToCartMain = document.getElementById("add-to-cart-main");

                    addToCartMain.addEventListener("click", (e) => {
                        e.preventDefault(); // Prevent default action first (best practice)
                        console.log(json.stock.id, document.getElementById("quantityInput").value);
                        addToCart(json.stock.id, document.getElementById("quantityInput").value);
                    
                    });

                    //add-to-cart-main-button-end

                    //similar-products

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
                             class="card-img-top rounded" alt="${product.title}" height="200px" />
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
                            <h5 class="mb-0">Rs. ${new Intl.NumberFormat("en-US", {minimumFractionDigits: 2}).format(stock.price)}</h5>
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
//                    });
                    });
                } else {
//                window.location = "index.html";
                    console.log("Unsucess1");
                }
            } else {


//                window.location = "index.html";
                console.log("Unsuccess2");
            }
        } catch (error) {
            console.log(error);
        } finally {

            animationend();
        }
    }
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