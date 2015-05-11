//
//  ParseManager.swift
//  MobiTrack
//
//  Created by Buğra Öner on 21.04.2015.
//  Copyright (c) 2015 Buğra Öner. All rights reserved.
//

import Foundation
import Parse

class ParseManager{
    
    var user: User?
    
    
    var userDefaults = NSUserDefaults.standardUserDefaults()
    
    init() {
        
    }
    
    func addUser(user: User) {
        var userTable = PFObject(className: "user")
        userTable["username"] = user.username!
        userTable["phone"] = user.phoneNo!
        userTable["lastLocation"] = user.lastLocation!
        userTable.save()
        
        var locationsTable = PFObject(className: "locations")
        locationsTable["user"] = user.phoneNo!
        locationsTable["locations"] = user.lastLocation!
        locationsTable.save()
    }
    
    func isUserExist() -> Bool {
        if userDefaults.objectForKey("userPhoneNo") == nil {
            return false
        }
        else {
            println("bu kullanii var")
            return true
        }
    }
    
    func getUsername(phoneNo: String) -> String {
        var query = PFQuery(className: "user")
        query.whereKey("phone", equalTo: phoneNo)
        var objects = query.findObjects()
        var username: String?
        for obj in objects! {
            username = obj["username"] as? String
        }
        return username!
    }
    
    func sendFriendshipRequest(recieverPhoneNo: String) {
        var friendshipTable = PFObject(className: "friendship")
        friendshipTable["sender"] = userDefaults.objectForKey("userPhoneNo") as! String
        friendshipTable["reciever"] = recieverPhoneNo
        friendshipTable["status"] = 0
        friendshipTable.save()
    }
    
    func isUserInParse(phoneNo: String) -> Bool {
        var query = PFQuery(className: "user")
        query.whereKey("phone", equalTo: phoneNo)
        var objects = query.findObjects()
        if objects?.count == 0 {
            return false
        }
        else {
            return true
        }
    }
    
    func getFriends(userPhoneNo: String) -> [User] {
        var users = [User]()
        var userPhoneNos: [String] = getFriendsPhoneNo(userPhoneNo)
        var query = PFQuery(className: "user")
        query.whereKey("phone", containedIn: userPhoneNos)
        var objects = query.findObjects()
        for obj in objects! {
            var user = User(phoneNo: obj["phone"] as! String!, username: obj["username"] as! String!, lastLocation: obj["lastLocation"] as! PFGeoPoint)
            users.append(user)
        }
        return users
    }
    
    func getFriendshipRequests(userPhoneNo: String) -> [User] {
        var friendshipRequests = [User]()
        var query = PFQuery(className: "friendship")
        query.whereKey("reciever", equalTo: userPhoneNo)
        query.whereKey("status", equalTo: 0)
        var objects = query.findObjects()
        
        var senderPhones: [String] = [String]()
        for obj in objects! {
            senderPhones.append(obj["sender"] as! String)
        }
        
        query = PFQuery(className: "user")
        query.whereKey("phone", containedIn: senderPhones)
        
        objects = query.findObjects()
        for obj in objects! {
            var user = User(phoneNo: obj["phone"] as! String!, username: obj["username"] as! String!, lastLocation: obj["lastLocation"] as! PFGeoPoint)
            friendshipRequests.append(user)
        }
        return friendshipRequests
    }
    
    func getFriendsPhoneNo(userPhoneNo: String) -> [String] {
        
        var userPhoneNos: [String] = [String]()
        var query = PFQuery(className: "friendship")
        var query2 = PFQuery(className: "friendship")
        
        query.whereKey("sender", equalTo: userPhoneNo)
        query.whereKey("status", equalTo: 2)
        var objects = query.findObjects()
        for obj in objects! {
            var objPhone: String = obj["reciever"] as! String
            userPhoneNos.append(objPhone)
        }
        
        query2.whereKey("reciever", equalTo: userPhoneNo)
        query2.whereKey("status", equalTo: 2)
        objects = query2.findObjects()
        for obj in objects! {
            var objPhone: String = obj["sender"] as! String
            userPhoneNos.append(objPhone)
        }
        return userPhoneNos
    }
    
    func getLocationsOfUser(userPhoneNo: String) -> [PFGeoPoint] {
        var locations: [PFGeoPoint] = [PFGeoPoint]()
        var lastLoc: PFGeoPoint?
        var query = PFQuery(className: "locations")
        query.whereKey("user", equalTo: userPhoneNo)
        var objects = query.findObjects()
        println(objects!.count)
        for obj in objects! {
            locations.append(obj["locations"]! as! PFGeoPoint)
        }
        return locations
    }
    
    func updateUserLocation(lastLocation: PFGeoPoint) {
        
        if (userDefaults.objectForKey("userPhoneNo") != nil) {
            
            var userPhoneNo: String = userDefaults.objectForKey("userPhoneNo") as! String
            var query = PFQuery(className: "user")
            query.whereKey("phone", equalTo: userPhoneNo)
            var objects = query.findObjects()
            for obj in objects! {
                var user: PFObject = obj as! PFObject
                user["lastLocation"] = lastLocation
                user.saveInBackground()
            }
            
            var locationsTable = PFObject(className: "locations")
            locationsTable["user"] = userPhoneNo
            locationsTable["locations"] = lastLocation
            locationsTable.save()
            
            println("LOCATION UPDATED")
        }
    }
    
    func applyFriendshipRequest(sender: String, reciever: String, status: Int) {
        var query = PFQuery(className: "friendship")
        query.whereKey("sender", equalTo: sender)
        query.whereKey("reciever", equalTo: reciever)
        
        var objects = query.findObjects()
        for obj in objects! {
            var friendship: PFObject = obj as! PFObject
            friendship["status"] = status
            friendship.saveInBackground()
        }
    }
    
    
}
