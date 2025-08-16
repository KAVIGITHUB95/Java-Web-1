async function adminSignIn() {

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const adminSignIn = {

        email: email,

        password: password
    };
    const signInJson = JSON.stringify(adminSignIn);

    const response = await fetch(
            "AdminSignIn",
            {
                method: "POST",
                body: signInJson,
                header: {
                    "Content-Type": "application/json"
                }
            }
    );
    if (response.ok) { // success
        const json = await response.json();

        if (json.status) { // if true
                window.location = "admin-dashboard.html";
        } else {// when status false
            alert(json.message);
        }
    } else {
        alert(json.message || "Sign In failed. Please try again");
    
    }

}

