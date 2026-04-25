package com.example.approvalsystem.service;

import com.example.approvalsystem.entity.User;
import com.example.approvalsystem.store.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private InMemoryDataStore dataStore;

    public User createUser(User user) {
        user.setUserId(dataStore.generateId());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return dataStore.saveUser(user);
    }

    public User updateUser(String userId, User user) {
        User existingUser = dataStore.getUserById(userId);
        if (existingUser == null) {
            return null;
        }
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getDepartment() != null) {
            existingUser.setDepartment(user.getDepartment());
        }
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        if (user.getDirectManagerId() != null) {
            existingUser.setDirectManagerId(user.getDirectManagerId());
        }
        existingUser.setUpdateTime(LocalDateTime.now());
        return dataStore.saveUser(existingUser);
    }

    public User getUserById(String userId) {
        return dataStore.getUserById(userId);
    }

    public List<User> getAllUsers() {
        return dataStore.getAllUsers();
    }

    public boolean deleteUser(String userId) {
        User user = dataStore.getUserById(userId);
        if (user == null) {
            return false;
        }
        dataStore.deleteUser(userId);
        return true;
    }
}
