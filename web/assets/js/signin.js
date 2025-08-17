async function signIn() {

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const signIn = {

        email: email,

        password: password
    };
    const signInJson = JSON.stringify(signIn);

    const response = await fetch(
            "SignIn",
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
            if (json.message === "1") {
                // Show success alert
                await showAlert("Sign In Success! Redirecting to verify your account...");

                // Redirect after 2 seconds
                setTimeout(() => {
                    
                    window.location = "verify-account.html";
                
                }, 2000);

            } else {
                // Show success alert
                await showAlert("Sign In Success!");

                
                // Redirect after 2 seconds
                
                setTimeout(() => {
                    window.location = "index.html";
                }, 2000);
            }
        } else { // when status false
            // Show error alert
            
            showAlert(json.message || "Sign In Failed. Please try again later!");
        }

    } else {

        showAlert(json.message || "Sign In failed. Please try again");
    }

}

async function authenticateUser() {

    const response = await fetch("SignIn");
    if (response.ok) {

        const json = await response.json();

        if (json.message === "1") {
            window.location = "index.html";
        }


    } else {

        alert(json.message || "Error authentication");
    }
}
