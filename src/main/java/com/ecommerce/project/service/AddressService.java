package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;

import java.util.List;

public interface AddressService {
    List<AddressDTO> getAddresses();
    AddressDTO getAddressById(Long addressId);
    List<AddressDTO> getUserAddresses(User user);
    AddressDTO createAddress(AddressDTO addressDTO, User user);
    AddressDTO updateAddress(Long addressId, AddressDTO addressDTO);
    AddressDTO deleteAddress(Long addressId);
}
