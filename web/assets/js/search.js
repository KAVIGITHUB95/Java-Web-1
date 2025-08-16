window.addEventListener("load", function () {
    loadData();
});

async function loadData() {
    const popup = Notification();
    const response = await fetch("LoadData");
    if (response.ok) {
        const json = await response.json();
        if (json.status) {

            console.log(json);

            loadOptions("brand", json.brandList, "name");
            console.log(json.brandList);
            loadOptions("category", json.categoryList, "name");

            loadOptions("color", json.colorList, "value");
            loadOptions("size", json.sizeList, "value");

            updateProductView(json);

        } else {
            alert(json.message || "Something went wrong");

        }
    } else {
        alert(json.message || "Something went wrong");

    }

}


function loadOptions(prefix, dataList, property) {
    const options = document.getElementById(`${prefix}-options`);
    options.innerHTML = ""; // Clear previous options

    dataList.forEach((item, index) => {
        const li = document.createElement("p");

        // Create checkbox input
        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.className = `${prefix}-checkbox form-check-input me-1`;
        checkbox.value = item[property];
        checkbox.id = `${prefix}-${index}`;

        // Create label
        const label = document.createElement("label");
        label.htmlFor = checkbox.id;
        label.textContent = item[property];

        // Handle single-check behavior
        checkbox.addEventListener("change", function () {
            const allCheckboxes = document.querySelectorAll(`#${prefix}-options input[type="checkbox"]`);
            allCheckboxes.forEach(cb => {
                if (cb !== this)
                    cb.checked = false;
            });
        });

        li.appendChild(checkbox);
        li.appendChild(label);
        options.appendChild(li);
    });
}

function getSelectedOption(prefix) {
    const selected = document.querySelector(`#${prefix}-options input[type="checkbox"]:checked`);
    return selected ? selected.value : null;
}

async function searchProduct(firstResult) {
    const brand_name = getSelectedOption("brand");
    console.log(brand_name); // e.g., "Apple"

    const category_name = getSelectedOption("category");
    console.log("Category:", category_name);

    const color_name = getSelectedOption("color");
    console.log("Color:", color_name);

    const size_value = getSelectedOption("size");
    console.log("Size:", size_value);

    const price_range_start = parseInt(minRange.value); //left
    console.log(price_range_start);
    const price_range_end = parseInt(maxRange.value); //right
    console.log(price_range_end);

    const sort_value = document.getElementById("st-sort").value;

    const data = {
        firstResult: firstResult,
        brandName: brand_name,
        categoryName: category_name,
        colorName: color_name,
        sizeValue: size_value,
        priceStart: price_range_start,
        priceEnd: price_range_end,
        sortValue: sort_value
    };

    const dataJSON = JSON.stringify(data);


    const response = await fetch("SearchProducts",
            {
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
            updateProductView(json);
            alert(json.message || "Product Filtering Complete..");
        } else {

            alert(json.message || "Something went wrong. Please try again later");

        }


    } else {

        alert(json.message || "Something went wrong. Please try again later");

    }
}

const st_product = document.getElementById("st-product"); // product card parent node
let st_pagination_button = document.getElementById("st-pagination-button");
let current_page = 0;

function updateProductView(json) {

    const productContainer = document.getElementById("product-container");
    const productTemplate = document.getElementById("product-template");
    productContainer.innerHTML = "";

    json.stockList.forEach(stock => {
        let clone = productTemplate.cloneNode(true);
        clone.style.display = "block";
//        clone.removeAttribute("id"); // Prevent duplicate IDs

        // Update dynamic content
        clone.querySelector("#product-a").href = "single-product.html?id=" + stock.id;
        clone.querySelector("#product-img").src = "product-images/" + stock.product.id + "/image1.png";
        clone.querySelector("#product-title").textContent = stock.product.title;
        clone.querySelector("#product-price").textContent = stock.price.toFixed(2);

        clone.querySelector("#product-add-to-cart").addEventListener("click", (e) => {
            addToCart(stock.id, 1);
            e.preventDefault();
        });

        clone.querySelector("#listDown").innerHTML = `<li class="d-flex justify-content-between"><span>Color:</span><span>${stock.color.value}</span></li>
                                            <li class="d-flex justify-content-between"><span>Size: </span><span>${stock.size.value}</span></li>
        <li class="d-flex justify-content-between"><span>Category: </span><span>${stock.product.category.name}</span></li>
`;

        // Append to container
        productContainer.appendChild(clone);
    });

    let st_pagination_container = document.getElementById("st-pagination-container");
    st_pagination_container.innerHTML = "";

    let all_product_count = json.allProductCount;
    let product_per_page = 6;
    let pages = Math.ceil(all_product_count / product_per_page);

    const createButton = (label, page, isActive = false) => {
        let btn = document.createElement("button");
        btn.textContent = label;
        btn.className = `btn btn-outline-primary mx-1 ${isActive ? 'active' : ''}`;
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            current_page = page;
            searchProduct(current_page * product_per_page);
        });
        return btn;
    };

// Prev button
    if (current_page > 0) {
        st_pagination_container.appendChild(createButton("Prev", current_page - 1));
    }

// Numbered buttons
    for (let i = 0; i < pages; i++) {
        st_pagination_container.appendChild(createButton(i + 1, i, i === current_page));
    }

// Next button
    if (current_page < pages - 1) {
        st_pagination_container.appendChild(createButton("Next", current_page + 1));
    }
}


async function addToCart(stockId, qty) {
    const popup = new Notification();
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

function clearAll() {
    loadData();
}