package com.example.smartcontactmanager.controller;

import com.example.smartcontactmanager.dao.UserRepository;
import com.example.smartcontactmanager.entities.User;
import com.example.smartcontactmanager.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class SmartController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/")
    public String homePage(Model model){
        model.addAttribute("title","Home - Smart Contact Manager");
        return "home";
    }

    @GetMapping("/about")
    public String aboutPage(Model model){
        model.addAttribute("title","About - Smart Contact Manager");
        return "about";
    }
    @GetMapping("/signup")
    public String singUpPage(Model model){
        model.addAttribute("title","Signup - Smart Contact Manager");
        model.addAttribute("user",new User());
        return "signup";
    }
    @PostMapping("/register")
    public String registration(@Valid @ModelAttribute("user") User user, BindingResult result, @RequestParam(value = "agreement",defaultValue = "false") boolean agreement, Model model, HttpSession session){
        try{
            model.addAttribute("title","Signup - Smart Contact Manager");
            if(!agreement){
                System.out.println("You have not agree to our Terms and Condition");
                throw new Exception("You have not agree to our Terms and Condition");
            }
            if(result.hasErrors()){
                System.out.println(result);
                model.addAttribute("user",user);
                return "signup";
            }
            user.setRole("ROLE_USER");
            user.setImageUrl("default.png");
            user.setEnabled(true);
            // saving encoded password to database.
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            System.out.println(user);
            userRepository.save(user);
            model.addAttribute("user",new User());
            session.setAttribute("msg",new Message("You have Successfully registered","alert-success"));
        }catch (Exception e){
            model.addAttribute("user",user);
            session.setAttribute("msg",new Message(e.getMessage(),"alert-danger"));
            e.printStackTrace();
        }
        return "signup";
    }

    @GetMapping("/login")
    public String loginPage(Model model){
        model.addAttribute("title","Login - Smart Contact Manager");
        return "login";
    }
}
