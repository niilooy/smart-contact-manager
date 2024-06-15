package com.example.smartcontactmanager.controller;

import com.example.smartcontactmanager.dao.UserRepository;
import com.example.smartcontactmanager.entities.User;
import com.example.smartcontactmanager.helper.Message;
import com.example.smartcontactmanager.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Random;

@Controller
public class ForgetController {

    Random random = new Random(1000);
    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/forget")
    public String forgetPasswordPage(Model model){
        model.addAttribute("title","Forget Password - Smart Contact Manager");
        model.addAttribute("submit_link","/send-otp");
        return "forgetPassword";
    }
    @PostMapping("/send-otp")
    public String forgetPassword(@RequestParam("email") String email, Model model, HttpSession session){
        model.addAttribute("title","Otp send - Smart Contact Manager");
        // generating random number..
        int otp = random.nextInt(999999);
        model.addAttribute("email_id",email);
        String message="<div style='border:2px solid #ccc; padding:16px'>" +
                "<h1>" +
                "Opt : <strong>" + otp +
                "</strong>"+
                "</h1>" +
                "</div>";
        boolean flag = emailService.sendEmail(email, "Forget Password OTP from SCM", message);
        if(flag){
            session.setAttribute("msg",new Message("OTP send successfully to "+email , "alert-success"));
            model.addAttribute("otp","otp generated successfully");
            model.addAttribute("submit_link","/verify-otp");
            session.setAttribute("otp",otp);
            System.out.println(email);
            System.out.println(otp);
            return "forgetPassword";
        }
        else{
         session.setAttribute("msg",new Message("Please enter correct email id" , "alert-danger"));
         return "redirect:/forget";
        }
    }

    @PostMapping("/verify-otp")
    public String VerifyOtp(@RequestParam("email") String email, @RequestParam("otp") int formOtp, Model model, HttpSession session){
        model.addAttribute("title","Verify Otp - Smart Contact Manager");
        // generating random number..
        User user = userRepository.getUserByUserName(email);

        int otp = (int)session.getAttribute("otp");
        if(otp != formOtp){
            session.setAttribute("msg",new Message("OTP does not match" , "alert-danger"));
            model.addAttribute("email_id",email);
            model.addAttribute("otp","otp generated successfully");
            return "forgetPassword";
        }

        if(user==null){
            session.setAttribute("msg",new Message("User with this email id does not exist" , "alert-warning"));
            return "forgetPassword";
        }
        else{
            session.setAttribute("email",email);
            return "changePassword";
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("newPassword") String newPassword, Model model, HttpSession session){
        // generating random number..
        String email = (String) session.getAttribute("email");
        User user = userRepository.getUserByUserName(email);

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);

            return "redirect:/login?resetPassword=Password changed successfully";
    }
}
