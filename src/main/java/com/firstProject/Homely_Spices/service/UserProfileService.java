package com.firstProject.Homely_Spices.service;

import com.firstProject.Homely_Spices.model.Address;
import com.firstProject.Homely_Spices.model.Users;
import com.firstProject.Homely_Spices.repo.AddressRepo;
import com.firstProject.Homely_Spices.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserProfileService {

    @Autowired
    AddressRepo addressRepo;

    @Autowired
    UserRepo userRepo;

    public List<Address> getUserAddress(String username){
       Optional<Users> user = userRepo.findByUsername(username);
       List<Address> userAddress = new ArrayList<>();
       int userId = user.get().getId();
       List<Address> allAddress = addressRepo.findAll();
       allAddress.stream().forEach(address->{
           int uid = address.getUser().getId();
           if(uid == userId){
               userAddress.add(address);
           }
       });
      return userAddress;
    }

    public void updateUser(Users updatedUser) {
        System.out.println("id="+updatedUser.getId());
        Users existingUser = userRepo.findById(updatedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + updatedUser.getId()));

        existingUser.setName(updatedUser.getName());
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        userRepo.save(existingUser);
    }

    public void updateAddress(Address address) {
       Address existingAddress = addressRepo.findById(address.getId()).orElseThrow(()-> new IllegalArgumentException("Address not found with Id: "+address.getId()));
        existingAddress.setHousename(address.getHousename());
        existingAddress.setStreet(address.getStreet());
        existingAddress.setCity(address.getCity());
        existingAddress.setState(address.getState());
        existingAddress.setCountry(address.getCountry());
        existingAddress.setZipcode(address.getZipcode());
       addressRepo.save(existingAddress);
    }

    public Address getAddressById(int id) {
        return addressRepo.findById(id).orElseThrow(()->new IllegalArgumentException("No address found with the ID"));

    }


    public boolean setSelectedAddress(String username, int addressId) {
        // Set the selected address for the user
        Users user = userRepo.findByUsername(username).orElseThrow(()->new RuntimeException("No user found!"));
        Address address = addressRepo.findByIdAndUser(addressId, user);
        return address != null;
    }

}
