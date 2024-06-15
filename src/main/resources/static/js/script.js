console.log("This is text come from script file");

let backdrop = document.querySelector(".backdrop");
let side_bar = document.querySelector(".side-bar");
let main_content = document.querySelector(".main-content");
let menu_bar = document.querySelector(".menu-bar");


menu_bar.addEventListener("click",()=>{
    console.log("menu-bar");
    backdrop.style.display="block";
    side_bar.style.display="block";
    main_content.style.display="none";
});

backdrop.addEventListener("click",()=>{
    console.log("backdrop");
    backdrop.style.display="none";
    side_bar.style.display="none";
    main_content.style.display="block";
})

// delete contact function
function deleteContact(cid) {
    swal({
        title: "Are you sure?",
        text: "Once deleted, you will not be able to recover this contact!",
        icon: "warning",
        buttons: true,
        dangerMode: true,
    })
        .then((willDelete) => {
            if (willDelete) {
                window.location = "/user/delete/"+cid;
            } else {
                swal("Your contact is safe!");
            }
        });
}

const search = ()=>{
    // console.log("searching...");
    let query = $("#search-input").val();

    if(query == ''){
        $(".search-result").hide();
    }
    else{
        // console.log(query);
        let url = `http://localhost:8080/search/${query}`;
        fetch(url).then((res)=>{
            return res.json();
        }).then((data)=>{
            // console.log(data);
            let result = `<ul class="list-group">`;
            data.forEach(contact => {
                result+=`<a href="/user/contact/${contact.cid}" class="list-group-item list-group-item-action">${contact.name}</a>`
            });
            
            result+="</ul>";
            $(".search-result").html(result);
            $(".search-result").show();
        })
        
    }


}

// Making Payment method...

// initiating payment...

// first request to server to create order

const paymentStart = ()=>{
    let amount = $("#payment_initiated").val();
    console.log(amount);
    if(amount == '' || amount == null){
        swal("Opps!!", "Please enter a valid amount and try again.", "error");
        return;
    }

    // we will use ajax to send request to server to create order
    $.ajax(
        {
            url: '/user/create_order',
            data:JSON.stringify({amount:amount,info:"order request"}),
            contentType:'application/json',
            type:'POST',
            dataType:'json',
            success:function(response){
                console.log(response);
                if(response.status=="created"){
                    // open payment form
                    let options = {
                        "key": "rzp_test_alpB49M5tUOFwh", // Enter the Key ID generated from the Dashboard
                        "amount": response.amount, // Amount is in currency subunits. Default currency is INR. Hence, 50000 refers to 50000 paise
                        "currency": "INR",
                        "name": "Smart Contact Manager",
                        "description": "Test Transaction",
                        "image": "https://w7.pngwing.com/pngs/612/213/png-transparent-payment-gateway-e-commerce-payment-system-payment-gateway-icon-blue-text-service.png",
                        "order_id": response.id, //This is a sample Order ID. Pass the `id` obtained in the response of Step 1
                        "handler": function (response){
                            console.log(response.razorpay_payment_id);
                            console.log(response.razorpay_order_id);
                            console.log(response.razorpay_signature);
                            SavePaymentDetailsOnServer(response.razorpay_payment_id,response.razorpay_order_id);
                            
                        },
                        "prefill": {
                            "name": "",
                            "email": "",
                            "contact": ""
                        },
                        "notes": {
                            "address": "Smart Contact Manager Corporate Office"
                        },
                        "theme": {
                            "color": "#3399cc"
                        }
                    }

                    let rzp = new Razorpay(options);
                    rzp.on('payment.failed', function (response){
                            console.log(response.error.code);
                            console.log(response.error.description);
                            console.log(response.error.source);
                            console.log(response.error.step);
                            console.log(response.error.reason);
                            console.log(response.error.metadata.order_id);
                            console.log(response.error.metadata.payment_id);
                            console.log("Payment failed");
                            swal("Opps!!", "Payment failed", "error");
                    });

                    rzp.open(); 
                }
            },
            error:function(error){
                console.log(error);
                swal("Something went wrong","please try again after some time","error");
            }
        }
    )


}


function SavePaymentDetailsOnServer(payment_id,order_id){
    $.ajax(
        {
            url: '/user/update_order',
            data:JSON.stringify({payment_id:payment_id,order_id:order_id,status:"paid"}),
            contentType:'application/json',
            type:'POST',
            dataType:'json',
            success:function(response){
                console.log("Payment successfully done...");
                swal("Congress ..", "Payment successfully done...", "success");
            },
            error:function(error){
                console.log(error);
                alert("Payment successfully done...","The payment is not reflected in our server please mail your payment screenshot to us.","success");
            }
        }
    )
}