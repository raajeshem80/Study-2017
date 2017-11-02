package com.kaluzny.web;

import com.kaluzny.domain.User;
import com.kaluzny.service.UserService;
import com.kaluzny.service.exception.UserAlreadyExistsException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.util.List;

@Service
@RestController("user")
@RequestMapping("/api/v1/")
@Api(value="some value", description="Testing")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private UserService userService;

    @Inject
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* Create a user */
    @ApiOperation(value = "Create a user",response = User.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    }
    )
    @RequestMapping(
            value = "objects",
            method = RequestMethod.POST)
    public ResponseEntity<User> createUser(@RequestBody User user, UriComponentsBuilder ucBuilder) {
        LOGGER.debug(">>> Creating user with id: " + user.getId());
        if (userService.isUserExist(user)) {
            LOGGER.debug("A user with name " + user.getId() + "exist.");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        userService.saveUser(user);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("user/{id}").buildAndExpand(user.getId()).toUri());
        return new ResponseEntity<>(user, headers, HttpStatus.CREATED);
    }

    /* Reading single user */
    @ApiOperation(value = "Search a user with an ID",response = User.class)
    @RequestMapping(
            value = "objects/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        LOGGER.debug("Fetching user with id: " + id);
        User user = userService.findUserById(id);
        if (user == null) {
            LOGGER.debug("User with id: " + id + ", not found!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /* Reads all users */
    @ApiOperation(value = "View a list of available users",response = Iterable.class)
    @RequestMapping(
            value = "objects",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> listAllUsers() {
        LOGGER.debug("Received request to list all users");
        List<User> users = userService.findAllUsers();
        if (users.isEmpty()) {
            LOGGER.debug("Users do not have.");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /* Update a user */
    @RequestMapping(
            value = "objects/{id}",
            method = RequestMethod.PUT)
    public ResponseEntity<User> updateUserFromDB(@PathVariable("id") long id,
                                                 @RequestBody User user) {
        LOGGER.debug(">>> Updating user with id: " + id);
        User currentUser = userService.findUserById(id);

        if (currentUser == null) {
            LOGGER.debug("<<< User with id: " + id + ", not found!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        currentUser.setTitle(user.getTitle());
        currentUser.setValue(user.getValue());

        userService.updateUser(currentUser);
        return new ResponseEntity<>(currentUser, HttpStatus.OK);
    }

    /* Delete a user */
    @RequestMapping(
            value = "objects/{id}",
            method = RequestMethod.DELETE)
    public ResponseEntity<User> deleteUserFromDB(@PathVariable("id") long id) {
        LOGGER.debug("Fetching & Deleting User with id: " + id + " is successfully removed from database!");

        User user = userService.findUserById(id);
        if (user == null) {
            LOGGER.debug("Unable to delete. User with id: " + id + ", not found!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /* Delete all users */
    @RequestMapping(
            value = "objects",
            method = RequestMethod.DELETE)
    public ResponseEntity<User> deleteAllUsers() {
        userService.deleteAllUsers();
        LOGGER.debug("Removed all users from database!");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleUserAlreadyExistsException(UserAlreadyExistsException exception) {
        return exception.getMessage();
    }
}