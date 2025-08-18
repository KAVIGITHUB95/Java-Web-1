async function loadGender(){
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

async function signUp(){
    
    const firstName = document.getElementById("firstName").value;
    const lastName = document.getElementById("lastName").value;
    const email = document.getElementById("email").value;
    const mobile = document.getElementById("mobile").value;
    const gender = document.getElementById("gender").value;
    const password = document.getElementById("password").value;
    
    const user = {
        firstName:firstName,
        lastName:lastName,
        email:email,
        mobile:mobile,
        gender:gender,
        password:password
    
    };
    
    const userJson = JSON.stringify(user);
    animationstart();
    const response = await fetch(
            "SignUp",
            { 
                method: "POST",
                body: userJson,
                headers: {
                    "Content-Type": "application/json"
                
                }
            }
    );
    
    if(response.ok){ // success
        const json = await response.json();
        
        if(json.status){ // if true
            await showAlert(json.message || "Registration is successful!", true);
            window.location = "verify-account.html";
        }else{// when status false
            // custom message
            
            await showAlert(json.message || "Registration failed. Please try again", false);
        }
    }else{
        
        await showAlert(json.message || "Registration failed. Please try again", false);
        
    }
}

