package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.repo.UserRepo;
import com.firstProject.Homely_Spices.service.MailService;
import com.firstProject.Homely_Spices.service.UserRedirectService;
import com.firstProject.Homely_Spices.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.firstProject.Homely_Spices.model.Users;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class LoginController {

    private final UserService userService;

    @Autowired
    UserRepo userRepo;

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    private final MailService mailService;
    private final UserRedirectService redirectService;

    private LoginController(UserService userService, MailService mailService, UserRedirectService redirectService) {
        this.userService = userService;
        this.mailService = mailService;
        this.redirectService = redirectService;
    }


    @GetMapping("/signup")
    public String signupPage(Model model){
        model.addAttribute("user",new Users());
        return "signup";
    }


    @GetMapping("/verifyOtp")
    public String verifyOtpPage(HttpSession session, Model model) {
        if (session.getAttribute("tempUser") == null) {
            return "redirect:/login";
        }
        model.addAttribute("session",session);
        return "verifyOtp";
    }

    @PostMapping("/signup")
    public String saveUser(@ModelAttribute Users user, HttpSession session,
                           RedirectAttributes redirectAttributes) throws IOException {


        if (userService.userExisting(user.getEmail())) {
            redirectAttributes.addFlashAttribute("emailError", "User already exists with this email!");
            return "redirect:/signup";
        }

        session.setAttribute("tempUser", user);
        mailService.generateAndSendOtp(user.getEmail(), session);

        return "redirect:/verifyOtp";
    }


    @PostMapping("/verifyOtp")
    public String veryOtpRequest(@RequestParam("otp") String otp, HttpSession session) {
        Users tempUser = (Users) session.getAttribute("tempUser");
        if (tempUser == null) {
            session.setAttribute("errorMsg", "Session expired. Please register again.");
            return "redirect:/signup";
        }

        try {
            String storedOtp = (String) session.getAttribute("otp"); // Store the OTP in session
            log.info("OTP entered: {}", otp);
            log.info("OTP stored in session: {}", storedOtp);

            otp = otp.replace(",", "");

            if (storedOtp == null || !storedOtp.equals(otp)) {
                session.setAttribute("errorMsg", "Invalid OTP. Please try again.");
                return "redirect:/verifyOtp";
            }

            Users savedUser = userService.saveUser(tempUser);
            System.out.println(savedUser);
            if (savedUser != null) {
                session.setAttribute("successMsg", "Registered successfully!");
                session.removeAttribute("tempUser");
                session.removeAttribute("otp");
                return "redirect:/login";
            } else {
                session.setAttribute("errorMsg", "Failed to save user. Please try again.");
                return "redirect:/signup";
            }
        } catch (Exception e) {
            log.error("Error during OTP verification: {}", e.getMessage());
            session.setAttribute("errorMsg", "An error occurred. Please try again later.");
        }

        return "redirect:/verifyOtp";
    }


    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        HttpSession session,
                        Model model) {
        if (error != null) {
            // Retrieve the error message from the session
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                model.addAttribute("errorMessage", errorMessage);
                session.removeAttribute("errorMessage"); // Clear the session attribute
                System.out.println("Retrieved errorMessage from session: " + errorMessage); // Debug log
            } else {
                model.addAttribute("errorMessage", "Invalid login credentials");
                System.out.println("No errorMessage found in session. Using default message."); // Debug log
            }
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out!");
        }

        return "login"; // Return the login page
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Manually invalidates the session
        return "redirect:/login?logout";
    }

    @GetMapping("/forgetPassword")
    public String forgetPasswordPage(Principal principal, @AuthenticationPrincipal OAuth2User user){
        String redirect = redirectService.redirectUserByRole(principal, user);
        if (redirect != null){
            return redirect;
        }
        return "forgot-password";
    }

    @PostMapping("/forget-password")
    public String forgetPasswordRequest(@RequestParam("email") String email, HttpSession session, RedirectAttributes attributes){
        if(!userService.userExisting(email)){
            attributes.addFlashAttribute("error", "user not found with email");
            return "redirect:/forgetPassword";
        }
        mailService.generateAndSendOtp(email,session);
        attributes.addFlashAttribute("email", email);
        return "redirect:/verifyotp-resetpassword";
    }


    @GetMapping("/verifyotp-resetpassword")
    public String verifyAndCreatePassword(Principal principal, @AuthenticationPrincipal OAuth2User user){
        String redirect = redirectService.redirectUserByRole(principal, user);
        if (redirect != null){
            return redirect;
        }
        return "verify-otp";
    }

    @PostMapping("/verify-and-create-password")
    public String verifyPasswordRequest(
            @RequestParam("pass") String newPass,
            @RequestParam("otp") String otp,
            @RequestParam("email") String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {

            mailService.verifyOtp(otp, session.getId());

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            userRepo.changePassword(email, encoder.encode(newPass));


            redirectAttributes.addFlashAttribute("success", "Password has been reset successfully.");


            return "redirect:/login";
        }catch (MailService.OtpTimeoutException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgetPassword";
        } catch (MailService.OtpVerificationException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgetPassword";
        }
        catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Something went wrong. Please try again.");
            return "redirect:/forgetPassword";
        }
    }

}
