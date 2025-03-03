package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Address;
import com.firstProject.Homely_Spices.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepo extends JpaRepository<Address,Integer> {
    Address findByIdAndUser(int addressId, Users user);
}
