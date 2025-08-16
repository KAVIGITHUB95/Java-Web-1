function loadUserData() {
    loadGender();
    getUserData();
    getCityData();
    loadOrder();
};

async function getUserData() {

    const response = await fetch("UserAccount");

    if (response.ok) {
        const json = await response.json();

        console.log(json);


        document.getElementById("username").innerHTML = `Hello, ${json.firstName} ${json.lastName}`;
        document.getElementById("since").innerHTML = `Member of KickCart since ${json.since}`;
        document.getElementById("firstName").value = json.firstName;
        document.getElementById("lastName").value = json.lastName;
        document.getElementById("email").value = json.email;
        document.getElementById("mobile").value = json.mobile;
        document.getElementById("gender").value = json.gender;
        document.getElementById("currentPassword").value = json.password;

        document.getElementById("newPassword").innerHTML = "";

        document.getElementById("confirmPassword").innerHTML = "";

        if (json.hasOwnProperty("addressList") && json.addressList !== undefined) {
            let email;
            let lineOne;
            let lineTwo;
            let city;
            let postalCode;
            let cityId;
            const addressUL = document.getElementById("addressUL");
            json.addressList.forEach(address => {
                email = address.user.email;
                lineOne = address.lineOne;
                lineTwo = address.lineTwo;
                city = address.city.name;
                postalCode = address.postalCode;

                cityId = address.city.id;

                addressUL.innerHTML = lineOne + ",<br/>" + lineTwo + ",<br/>" + city + "<br/>" + postalCode;
            });

            document.getElementById("lineOne").value = lineOne;
            document.getElementById("lineTwo").value = lineTwo;
            document.getElementById("postalCode").value = postalCode;
            document.getElementById("city").value = cityId;

        }

    }


}
;

async function getCityData() {

    const response = await fetch("LoadCityData");
    if (response.ok) {

        const json = await response.json();

        const citySelect = document.getElementById("city");
        json.forEach(city => {
            let option = document.createElement("option");
            option.innerHTML = city.name;
            option.value = city.id;
            citySelect.appendChild(option);

        });

    }
};

async function loadGender() {
    const genderSelect = document.getElementById("gender");

    const response = await fetch("LoadGender");
    animationstart();
    if (response.ok) {
        const json = await response.json();


        json.forEach(gender => {
            let option = document.createElement("option");
            option.innerText = gender.name;
            option.value = gender.id;
            option.className = "form-select";
            genderSelect.appendChild(option);

        });
        animationend();
    }
}


async function saveUser() {
    event.preventDefault();
    const popup = new Notification();
    const firstName = document.getElementById("firstName").value;
    const lastName = document.getElementById("lastName").value;
    const email = document.getElementById("email").value;
    const mobile = document.getElementById("mobile").value;
    const genderId = document.getElementById("gender").value;
    const profileImage = document.getElementById("profile-image").files[0];

    const form = new FormData();

    form.append("action", "updateUserInfo");
    form.append("firstName", firstName);
    form.append("lastName", lastName);
    form.append("email", email);
    form.append("mobile", mobile);
    form.append("genderId", genderId);
    form.append("profileImage", profileImage);

    const response = await fetch("UserAccount", {
        method: "POST",
        body: form
    });

    if (response.ok) {
        const json = await response.json();
        console.log(json);
        if (json.status) {
            showAlert(json.message);
            getUserData();

        } else {
            showAlert(json.message);
//            alert(json.message);
        }
    } else {
        showAlert("Profile details update failed!");
//        alert("Profile details update failed!");
    }
}


async function saveAddress() {
    event.preventDefault();
    const lineOne = document.getElementById("lineOne").value;
    const lineTwo = document.getElementById("lineTwo").value;
    const postalCode = document.getElementById("postalCode").value;
    const cityId = document.getElementById("city").value;


    const addressDataObject = {
        action: "addAddress",
        lineOne: lineOne,
        lineTwo: lineTwo,
        postalCode: postalCode,
        cityId: cityId
    };

    const addressDataJSON = JSON.stringify(addressDataObject);

    const response = await fetch("UserAccount", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },

        body: addressDataJSON
    });

    if (response.ok) {
        const json = await response.json();
        if (json.status) {
            console.log(json);
            alert(json.message);
            getUserData();
        } else {
            console.log(json);
            alert(json.message);
        }
    } else {
        console.log(json);
        alert(json.message);
    }
}

async function savePassword() {


    const currentPassword = document.getElementById("currentPassword").value;

    const newPassword = document.getElementById("newPassword").value;

    const confirmPassword = document.getElementById("confirmPassword").value;

    const passwordDataObject = {
        currentPassword: currentPassword,
        newPassword: newPassword,

        confirmPassword: confirmPassword
    };

    const passwordDataJSON = JSON.stringify(passwordDataObject);

    const response = await fetch("UserAccount", {

        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },

        body: passwordDataJSON
    });

    if (response.ok) {

        const json = await response.json();

        if (json.status) {
            console.log(json);
            alert(json.message);
            getUserData();
        } else {
            console.log(json);
            alert(json.message);
        }
    } else {
        console.log(json);
        alert(json.message);
    }

}

async function loadOrder() {
    const container = document.getElementById("orders-container");
    const response = await fetch("LoadOrder");
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
                            ${order.address?.lineOne || ""} ${order.address?.lineTwo || ""}<br>
                            ${order.address?.city?.name || ""}, ${order.address?.postalCode || ""}                            
                        </p>
                        <p>
                            Status: ${order.order_status.value || ""}
                        </p>

                        <h6>Items:</h6>
                        <div class="table-responsive">
                            <table class="table table-striped table-sm">
                                <thead>
                                    <tr>
                                        <th>Product</th>
                                        <th>Qty</th>
                                        
                                        <th>Rating</th>
                                        <th>Price</th>
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
                        <td>${item.rating || 0}</td>
                        <td>${item.price || 'N/A'}</td>
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

async function saveChanges() {

    const userDataObject = {

        firstName: firstName,
        lastName: lastName,
        email: email,
        mobile: email,
        genderId: genderId,

        currentPassword: currentPassword,
        newPassword: newPassword,

        confirmPassword: confirmPassword

    };

    const userDataJSON = JSON.stringify(userDataObject);

    const response = await fetch("MyAccount", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"

        },
        body: userDataJSON
    });

    if (response.ok) {
        const json = await response.json();
        if (json.status) {
            getUserData();

        } else {
            document.getElementById("message").innerHTML = json.message;
        }
    } else {

        document.getElementById("message").innerHTML = "Profile details update failed!";
    }

}
;
