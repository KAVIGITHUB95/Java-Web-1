window.addEventListener("load", function () {
    animationstart();
    loadHeader();
    loadFooter();
    animationend();
});

async function loadHeader() {
    animationstart();

    const response = await fetch("CheckUser", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        }
    });

    const data = `
        <nav class="navbar navbar-expand-xl mt-2 p-4">
            <a class="navbar-brand mx-4 text-info" href="index.html">KickCart</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse justify-content-center" id="navbarContent">
                <ul class="navbar-nav justify-content-center flex-grow-1 ps-4">
                    <hr>
                    <li class="nav-item me-2 sign-links"><a class="nav-link text-underline" href="signin.html">Sign In</a></li>
                    <li class="nav-item me-2 sign-links"><a class="nav-link text-underline" href="signup.html">Sign Up</a></li>
                    <li class="nav-item me-2"><a class="nav-link text-underline" href="#">Men</a></li>
                    <li class="nav-item me-2"><a class="nav-link text-underline" href="#">Women</a></li>
                    <li class="nav-item me-2"><a class="nav-link text-underline" href="#">Sneakers</a></li>
                    <li class="nav-item me-2"><a class="nav-link text-underline" href="#">Sandals & Slippers</a></li>
                    <li class="nav-item me-2"><a class="nav-link text-underline" href="#">Running & Sports</a></li>
                    <li class="nav-item me-2"><a class="nav-link text-underline" href="#">Formal</a></li>
                    <li class="nav-item me-2"><a class="nav-link text-underline" href="#">Kids</a></li>
                    <hr>
                </ul>

                <ul class="navbar-nav ms-auto d-none d-xl-flex">
                    <li class="nav-item me-2"><a class="nav-link popup1" href="search.html" data-tooltip="Search"><i class="bi bi-search fs-4"></i></a></li>
                    <li class="nav-item me-2"><a class="nav-link popup1" href="user-account.html" data-tooltip="Account"><i class="bi bi-person-circle fs-4"></i></a></li>
                    <li class="nav-item me-2"><a class="nav-link popup1" href="cart.html" data-tooltip="Cart"><i class="bi bi-cart fs-4"></i></a></li>
                </ul>

                <ul class="navbar-nav ms-auto d-xl-none p-4">
                    <li class="nav-item me-2"><a class="nav-link" href="search.html">Search <i class="bi bi-search"></i></a></li>
                    <li class="nav-item me-2"><a class="nav-link" href="user-account.html">My Account <i class="bi bi-person-circle"></i></a></li>
                    <li class="nav-item me-2"><a class="nav-link" href="cart.html">Cart <i class="bi bi-cart"></i></a></li>
                </ul>
            </div>
        </nav>
    `;

    if (response.ok) {
        const json = await response.json();
        document.getElementById("header").innerHTML = data;

        if (json.status) {
            // Hide sign in/up if session valid
            document.querySelectorAll(".sign-links").forEach(el => el.style.display = "none");
            console.log("Cookie check:", json.message);
        } else {
            // Show sign in/up
            document.querySelectorAll(".sign-links").forEach(el => el.style.display = "block");
            console.log("No cookie:", json.message);
        }
    } else {
        // fallback if fetch fails
        document.getElementById("header").innerHTML = data;
    }

    animationend();
}


function loadFooter() {
    animationstart();
    const data1 = `<footer class="bg-dark text-light pt-5 pb-3 mt-5">
    <div class="container">
        <div class="row">
            <!-- Brand Info -->
            <div class="col-md-4 mb-4">
                <h5 class="text-uppercase fw-bold">KickCart</h5>
                <p>Your one-stop shop for the latest trends and unbeatable deals. Shop smart, shop KickCart.</p>
                <div>
                    <a href="#" class="text-light me-3"><i class="fab fa-facebook-f"></i></a>
                    <a href="#" class="text-light me-3"><i class="fab fa-instagram"></i></a>
                    <a href="#" class="text-light me-3"><i class="fab fa-twitter"></i></a>
                    <a href="#" class="text-light"><i class="fab fa-linkedin-in"></i></a>
                </div>
            </div>

            <!-- Quick Links -->
            <div class="col-md-2 mb-4">
                <h6 class="fw-bold">Quick Links</h6>
                <ul class="list-unstyled">
                    <li><a href="#" class="text-light text-decoration-none">Home</a></li>
                    <li><a href="#" class="text-light text-decoration-none">Shop</a></li>
                    <li><a href="#" class="text-light text-decoration-none">Deals</a></li>
                    <li><a href="#" class="text-light text-decoration-none">Contact</a></li>
                </ul>
            </div>

            <!-- Customer Service -->
            <div class="col-md-3 mb-4">
                <h6 class="fw-bold">Customer Service</h6>
                <ul class="list-unstyled">
                    <li><a href="#" class="text-light text-decoration-none">FAQs</a></li>
                    <li><a href="#" class="text-light text-decoration-none">Shipping & Returns</a></li>
                    <li><a href="#" class="text-light text-decoration-none">Order Tracking</a></li>
                    <li><a href="#" class="text-light text-decoration-none">Privacy Policy</a></li>
                </ul>
            </div>

            <!-- Newsletter -->
            <div class="col-md-3 mb-4">
                <h6 class="fw-bold">Subscribe to Our Newsletter</h6>
                <form>
                    <div class="input-group">
                        <input type="email" class="form-control" placeholder="Your Email">
                        <button class="btn btn-warning" type="submit">Subscribe</button>
                    </div>
                </form>
            </div>
        </div>

        <hr class="bg-light">

        <!-- Copyright -->
        <div class="text-center">
            <p class="mb-0">&copy; 2025 KickCart. All rights reserved.</p>
        </div>
    </div>
</footer>
            <!-- Navbar End -->`;
    document.getElementById("footer").innerHTML = data1;
    animationend();
}