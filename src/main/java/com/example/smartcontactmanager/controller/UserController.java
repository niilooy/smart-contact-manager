package com.example.smartcontactmanager.controller;

import com.example.smartcontactmanager.dao.ContactRepository;
import com.example.smartcontactmanager.dao.OrdersRepository;
import com.example.smartcontactmanager.dao.UserRepository;
import com.example.smartcontactmanager.entities.Contact;
import com.example.smartcontactmanager.entities.Orders;
import com.example.smartcontactmanager.entities.User;
import com.example.smartcontactmanager.helper.Message;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private static Map<String, String> getRzpKeys() {
        Map<String, String> map = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("rzp.csv"));
    
            // Skip the header
            lines.remove(0);
    
            for (String line : lines) {
                String[] parts = line.split(",");
                map.put("key_id", parts[0]);
                map.put("key_secret", parts[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
    

    @ModelAttribute
    public void addCommonDataToModal(Model model , Principal principal){
        /* here Principal is a java security package class in with help of this class object we can get user name after its login*/
        String name = principal.getName();
        // getting user details from the database.
        User user = userRepository.getUserByUserName(name);
        model.addAttribute("user",user);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model , Principal principal)
    {
        model.addAttribute("title","Dashboard - Smart Contact Manager");
        return "user/dashboard";
    }

//    Show Add Contact Page

    @GetMapping("/add-contact")
    public String openAddFormPage(Model model , Principal principal)
    {
        model.addAttribute("title","Add Contact - Smart Contact Manager");
        model.addAttribute("title_of_page","Add Contact");
        model.addAttribute("contact",new Contact());
        model.addAttribute("form_process_url","process-add-contact");
        return "user/add_contact_form";
    }

//    Saving Contact Details

    @PostMapping("/process-add-contact")
    public String saveContactDetails(@Valid @ModelAttribute("contact") Contact contact, BindingResult result, @RequestParam("profilePicture") MultipartFile profile, Model model, Principal principal, HttpSession session)
    {
        try{
            if(result.hasErrors()){
                System.out.println(result);
                model.addAttribute("contact",contact);
                return "user/add_contact_form";
            }

            String name = principal.getName();
            User user = userRepository.getUserByUserName(name);
            System.out.println(contact);

            if(profile.isEmpty()){
                System.out.println("file is empty");
                contact.setImage("default.png");
                session.setAttribute("msg",new Message("Profile Picture is required!!","alert-warning"));
            }
            else{
                File file = new ClassPathResource("static/images/profile").getFile();
                Path path = Paths.get(file.getAbsolutePath() + File.separator +contact.getNickName()+"_"+ profile.getOriginalFilename());
                contact.setImage(contact.getNickName()+"_"+ profile.getOriginalFilename());
                Files.copy(profile.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File uploaded");
            }
            session.setAttribute("msg",new Message("Your contact added successfully!!","alert-success"));
            contact.setUser(user);
            user.getContacts().add(contact);
            contactRepository.save(contact);
            model.addAttribute("contact",new Contact());
        }
        catch (Exception e){
            e.printStackTrace();
            session.setAttribute("msg",new Message("Some thing went woring please try again after some time!!","alert-danger"));
        }
        return "user/add_contact_form";
    }

//    show View Contact page
    @GetMapping("/view-contacts/{currentPage}")
    public String openViewContactPage(@PathVariable("currentPage") Integer currentPage ,Model model , Principal principal)
    {
        model.addAttribute("title","View Contact - Smart Contact Manager");
        // getting user
        String name = principal.getName();
        User user= userRepository.getUserByUserName(name);
        // getting contact details
        int perPage=1;
//        currentPage =start with 0
        Pageable pageable = PageRequest.of(currentPage, 5);
        Page<Contact> contacts = contactRepository.findAllContactsByUserId(user.getId(),pageable);
        model.addAttribute("contacts",contacts);
        model.addAttribute("currentPage",currentPage);
        model.addAttribute("totalPage",contacts.getTotalPages());
        return "user/view_contacts";
    }

    // Show contact Details page
    @GetMapping("/contact/{contact_id}")
    public String viewContactDetailsPage(@PathVariable("contact_id") Integer contact_id ,Model model , Principal principal)
    {
        model.addAttribute("title","Contact Details - Smart Contact Manager");
        Optional<Contact> contactOptional = contactRepository.findById(contact_id);
        Contact contact = contactOptional.get();

        // getting login user.
        String name = principal.getName();
        User user = userRepository.getUserByUserName(name);
        if(user.getId() == contact.getUser().getId()){
            model.addAttribute("contact",contact);
        }
        return "user/view_contact_details";
    }

//    delete Contact

    @GetMapping("/delete/{contact_id}")
    public String deleteContact(@PathVariable("contact_id") Integer contact_id ,Model model , Principal principal,HttpSession session)
    {
        Contact contact = contactRepository.findById(contact_id).get();

        // getting login user.
        String name = principal.getName();
        User user = userRepository.getUserByUserName(name);
        if(contact.getUser().getId() == user.getId()){
            contactRepository.delete(contact);
            File file = null;
            try {
                if(!contact.getImage().equals("default.png")){
                    file = new ClassPathResource("static/images/profile").getFile();
                    Path path = Paths.get(file.getAbsolutePath() + File.separator +contact.getImage());
                    Files.delete(path);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            session.setAttribute("msg", new Message("Your contact has been deleted successfully!!","alert-success"));
        }
        return "redirect:/user/view-contacts/0";
    }

//    Update Contact
    @GetMapping("/update-contact/{contact_id}")
    public String openUpdateContactPage(@PathVariable("contact_id") Integer contact_id , Model model , Principal principal)
    {
        model.addAttribute("title","Add Contact - Smart Contact Manager");
        model.addAttribute("title_of_page","Update Your Contact");
        String name = principal.getName();
        User user = userRepository.getUserByUserName(name);
        Contact contact = contactRepository.findById(contact_id).get();
        if(user.getId() == contact.getUser().getId()){
            model.addAttribute("contact",contact);
            model.addAttribute("form_process_url","process-update-contact");
            model.addAttribute("update_profile_picture_msg","(Choose only if you want to update)");
        }
        return "user/add_contact_form";
    }

    @PostMapping("/process-update-contact")
    public String UpdateContact(@ModelAttribute Contact contact, Model model , Principal principal,@RequestParam("cId") Integer cId,@RequestParam("profilePicture") MultipartFile profile,HttpSession session)
    {
        String name = principal.getName();
        User user = userRepository.getUserByUserName(name);
        Contact old_contact = contactRepository.findById(cId).get();
        if(user.getId() == old_contact.getUser().getId()){
            if(profile.isEmpty()){
                contact.setImage(old_contact.getImage());
            }
            else{
                File file = null;
                try {
                    file = new ClassPathResource("static/images/profile").getFile();
                // deleting old profile picture
                    if(!old_contact.getImage().equals("default.png")) {
                        Path path = Paths.get(file.getAbsolutePath() + File.separator + old_contact.getImage());
                        Files.delete(path);
                    }
                // updating new profile picture
                    Path path = Paths.get(file.getAbsolutePath() + File.separator +contact.getNickName()+"_"+ profile.getOriginalFilename());
                    contact.setImage(contact.getNickName()+"_"+ profile.getOriginalFilename());
                    Files.copy(profile.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Profile Picture updated");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            contact.setUser(user);
            contactRepository.save(contact);
            session.setAttribute("msg",new Message("Contact Details are Updated Successfully","alert-success"));
        }
        return "redirect:/user/view-contacts/0";
    }

    // showing profile page
    @GetMapping("/view-profile")
    public String showUserProfile(Model model){
        model.addAttribute("title","Profile - Smart Contact Manager");
        return "user/profile";
    }

    // update user profile picture
    @PostMapping("/update-profile-picture")
    public String updateProfilePicture(@RequestParam("userProfilePicture") MultipartFile userProfilePicture,Model model,Principal principal,HttpSession session){
        model.addAttribute("title","Profile - Smart Contact Manager");
        String name = principal.getName();
        User user = userRepository.getUserByUserName(name);
        if(userProfilePicture.isEmpty()){
            user.setImageUrl(user.getImageUrl());
        }
        else{
            File file = null;
            try {
                file = new ClassPathResource("static/images/userProfile").getFile();
                // deleting old profile picture
                if(!user.getImageUrl().equals("default.png")) {
                    Path path = Paths.get(file.getAbsolutePath() + File.separator + user.getImageUrl());
                    Files.delete(path);
                }
                // updating new profile picture
                Path path = Paths.get(file.getAbsolutePath() + File.separator +user.getId()+"_"+ userProfilePicture.getOriginalFilename());
                user.setImageUrl(user.getId()+"_"+ userProfilePicture.getOriginalFilename());
                Files.copy(userProfilePicture.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("User Profile Picture updated");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        userRepository.save(user);
        session.setAttribute("msg",new Message("Profile picture has been Updated Successfully","alert-success"));
        return "redirect:/user/view-profile";
    }


    // setting page..
    @GetMapping("/settings")
    public String settings(Model model){
        model.addAttribute("title","Settings - Smart Contact Manager");
        return "user/settings";
    }

    // change password..
    @PostMapping("/change-password")
    public String ChangePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session){
        //getting current user..
        String name = principal.getName();
        User user = userRepository.getUserByUserName(name);
        if(bCryptPasswordEncoder.matches(oldPassword,user.getPassword())){
            user.setPassword(bCryptPasswordEncoder.encode(newPassword));
            userRepository.save(user);
            session.setAttribute("msg", new Message("Your password has been changed successfully!!","alert-success"));
        }
        else{
            System.out.println("old password does not match.");
            session.setAttribute("msg", new Message("Please enter correct old password","alert-warning"));
            return "redirect:/user/settings";
        }
        return "redirect:/user/dashboard";
    }
    // Making payment method...

    @PostMapping("/create_order")
    @ResponseBody
    public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws RazorpayException, IOException {
        System.out.println(data);
        // getting amount
        int amount = Integer.parseInt(data.get("amount").toString());
        Map<String, String> keys = getRzpKeys();
        RazorpayClient razorpayClient = new RazorpayClient(keys.get("key_id"), keys.get("key_secret"));
        //System.out.printf(keys.get("key_id"), keys.get("key_secret"));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount",amount*100); // here we are multiplying with 100 so that rupees can be converted to paise.
        jsonObject.put("currency","INR");
        jsonObject.put("receipt","txn_345839");

        // creating new order..
        Order order = razorpayClient.orders.create(jsonObject);
        System.out.println(order);

        // saving order details to database.
        Orders orders = new Orders();
        orders.setOrderId(order.get("id"));
        orders.setAmount(order.get("amount"));
        orders.setPaymentId(null);
        orders.setStatus(order.get("status"));
        orders.setUser(userRepository.getUserByUserName(principal.getName()));
        ordersRepository.save(orders);

        return order.toString();
    }

    @PostMapping("/update_order")
    @ResponseBody
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) throws Exception {
        System.out.println(data);
        Orders orders = ordersRepository.findByOrderId(data.get("order_id").toString());
        orders.setPaymentId(data.get("payment_id").toString());
        orders.setOrderId(data.get("order_id").toString());
        orders.setStatus("paid");
        ordersRepository.save(orders);

        return ResponseEntity.ok(Map.of("msg","updated"));
    }



}
