//
//  FriendListTableViewController.swift
//  MobiTrack
//
//  Created by Buğra Öner on 21.04.2015.
//  Copyright (c) 2015 Buğra Öner. All rights reserved.
//

import UIKit
import Parse
import CoreLocation
import Foundation

class FriendListTableViewController: UITableViewController, CLLocationManagerDelegate {
    
    var locationUpdated: Bool = false
    
    var parseManager: ParseManager = ParseManager()
    var locationManager = CLLocationManager()
    var userDefaults = NSUserDefaults.standardUserDefaults()
    
    var userPhoneNo: String = String()
    var friendList: [User] = [User]()
    var friendshipRequests: [User] = [User]()
    
    var locationOfUser: PFGeoPoint = PFGeoPoint()

    override func viewDidLoad() {
        super.viewDidLoad()
        initializeLocationManager()
        
        if !parseManager.isUserExist() {
            enterUserInfoAlert()
        }
        else {
            userPhoneNo = userDefaults.objectForKey("userPhoneNo") as! String
        }
        friendList = parseManager.getFriends(userPhoneNo)
        friendshipRequests = parseManager.getFriendshipRequests(userPhoneNo)
    }
    
    func enterUserInfoAlert() {
        
        var usernameTextField: UITextField?
        var phoneNoTextField: UITextField?
        let alertController = UIAlertController(title: "Enter Your Info", message: "", preferredStyle: .Alert)
        
        alertController.addTextFieldWithConfigurationHandler { (textField) -> Void in
            usernameTextField = textField
            usernameTextField?.placeholder = "Username"
        }
        
        alertController.addTextFieldWithConfigurationHandler { (textField) -> Void in
            phoneNoTextField = textField
            phoneNoTextField?.placeholder = "Phone Number"
        }
        
        
        var usernameAction = UIAlertAction(title: "OK", style: UIAlertActionStyle.Default) { UIAlertAction in
            var user = User(phoneNo: phoneNoTextField!.text, username: usernameTextField!.text, lastLocation: self.locationOfUser)
            self.userPhoneNo = phoneNoTextField!.text
            self.parseManager.addUser(user)
            self.userDefaults.setObject(phoneNoTextField!.text, forKey: "userPhoneNo")
            self.userDefaults.synchronize()
        }
        alertController.addAction(usernameAction)
        self.presentViewController(alertController, animated: true, completion: nil)
        self.tableView.reloadData()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func locationManager(manager: CLLocationManager!, didUpdateLocations locations: [AnyObject]!) {
        CLGeocoder().reverseGeocodeLocation(manager.location, completionHandler: { (placemarks, error) -> Void in
            let placemark = placemarks[0] as! CLPlacemark
            self.setLocation(placemark)
        })
    }

    // MARK: - Table view data source

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        if section == 0 {
            return self.friendList.count
        }
        return self.friendshipRequests.count
    }
    
    override func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        if section == 0 {
            return "Friends"
        }
        return "Friendship Requests"
    }

    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("MyCell", forIndexPath: indexPath) as! UITableViewCell
        
        if indexPath.section == 0 {
            if self.locationUpdated {
                cell.textLabel?.text = friendList[indexPath.row].username
                cell.detailTextLabel?.text = self.getDistanceBetweenTwoLocation(locationOfUser, location2: friendList[indexPath.row].lastLocation!)
            }
            else {
                cell.textLabel?.text = ""
                cell.detailTextLabel?.text = ""
            }

        }
        else {
            cell.textLabel?.text = friendshipRequests[indexPath.row].username
        }
        return cell
    }


    /*
    // Override to support conditional editing of the table view.
    override func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return NO if you do not want the specified item to be editable.
        return true
    }
    */

    /*
    // Override to support editing the table view.
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            // Delete the row from the data source
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        } else if editingStyle == .Insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }
    */

    /*
    // Override to support rearranging the table view.
    override func tableView(tableView: UITableView, moveRowAtIndexPath fromIndexPath: NSIndexPath, toIndexPath: NSIndexPath) {

    }
    */

    /*
    // Override to support conditional rearranging of the table view.
    override func tableView(tableView: UITableView, canMoveRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return NO if you do not want the item to be re-orderable.
        return true
    }
    */

    
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        let detailCell = sender as! UITableViewCell
        let index = self.tableView.indexPathForCell(detailCell)?.row
        var dvc = segue.destinationViewController as! UsersLastLocationsViewController
        dvc.user = friendList[index!]
    }
    
    
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
    }
    
    override func tableView(tableView: UITableView, editActionsForRowAtIndexPath indexPath: NSIndexPath) -> [AnyObject]? {
        var emergencyFriendAction: UITableViewRowAction?
        
        var friendshipRejectAction: UITableViewRowAction?
        var friendshipAcceptAction: UITableViewRowAction?
        
        if indexPath.section == 0 {
            
            if self.isEmergencyFriend(self.friendList[indexPath.row].phoneNo!) {
                emergencyFriendAction = UITableViewRowAction(style: UITableViewRowActionStyle.Normal, title: "Remove Emergency Friend") { (tableViewAction, indexPath) -> Void in
                    tableView.editing = false
                    self.removeEmergencyFriend(self.friendList[indexPath.row].phoneNo!)
                }
            }
            else {
                emergencyFriendAction = UITableViewRowAction(style: UITableViewRowActionStyle.Default, title: "Add Emergency Friend") { (tableViewAction, indexPath) -> Void in
                    tableView.editing = false
                    self.addEmergencyFriend(self.friendList[indexPath.row].phoneNo!)
                }
                
            }
            return [emergencyFriendAction!]

        }
        else {
            friendshipRejectAction = UITableViewRowAction(style: UITableViewRowActionStyle.Default, title: "No", handler: { (tableViewAction, indexPath) -> Void in
                println("reddedildi")
                tableView.editing = false
                println("reddedilen: \(indexPath.row)")
                self.parseManager.applyFriendshipRequest(self.friendshipRequests[indexPath.row].phoneNo!, reciever: self.userPhoneNo, status: 1)
                self.friendshipRequests.removeAtIndex(indexPath.row)
                self.tableView.reloadData()
            })
            friendshipAcceptAction = UITableViewRowAction(style: UITableViewRowActionStyle.Normal, title: "Yes", handler: { (tableViewAction, indexPath) -> Void in
                println("kabul edildi")
                tableView.editing = false
                self.parseManager.applyFriendshipRequest(self.friendshipRequests[indexPath.row].phoneNo!, reciever: self.userPhoneNo, status: 2)
                self.friendList.append(self.friendshipRequests.removeAtIndex(indexPath.row))
                self.tableView.reloadData()
            })
            
            return [friendshipRejectAction!, friendshipAcceptAction!]
        }
    }
    
    func isEmergencyFriend(friendPhoneNo: String) -> Bool {
        var SOSFriends: [String] = [String]()
        if userDefaults.objectForKey("emergencyFriends") == nil {
            userDefaults.setObject(SOSFriends, forKey: "emergencyFriends")
            userDefaults.synchronize()
            return false
        }
        else {
            SOSFriends = userDefaults.objectForKey("emergencyFriends") as! [String]
            if contains(SOSFriends, friendPhoneNo) {
                return true
            }
            else {
                return false
            }
        }
    }
    
    func addEmergencyFriend(friendPhoneNo: String) {
        var SOSFriends: [String] = [String]()
        SOSFriends = userDefaults.objectForKey("emergencyFriends") as! [String]
        SOSFriends.append(friendPhoneNo)
        userDefaults.setObject(SOSFriends, forKey: "emergencyFriends")
        userDefaults.synchronize()
    }

    func removeEmergencyFriend(friendPhoneNo: String) {
        var SOSFriends: [String] = [String]()
        SOSFriends = userDefaults.objectForKey("emergencyFriends") as! [String]
        var index: Int = find(SOSFriends, friendPhoneNo)!
        SOSFriends.removeAtIndex(index)
        userDefaults.setObject(SOSFriends, forKey: "emergencyFriends")
        userDefaults.synchronize()
    }
    
    func initializeLocationManager() {
        self.locationManager.delegate = self
        self.locationManager.desiredAccuracy = kCLLocationAccuracyBest
        self.locationManager.startUpdatingLocation()
        self.locationManager.requestAlwaysAuthorization()
    }
    
    func setLocation(placemark: CLPlacemark)  {
        self.locationOfUser = PFGeoPoint(latitude: placemark.location!.coordinate.latitude, longitude: placemark.location!.coordinate.longitude)
        self.locationManager.stopUpdatingLocation()
        
        if !self.locationUpdated {
            self.parseManager.updateUserLocation(self.locationOfUser)
            println("location degisti")
            self.locationUpdated = true
            var currentLocation = CLLocationCoordinate2D(latitude: self.locationOfUser.latitude, longitude: self.locationOfUser.longitude)
            CurrentLocation.lastLocation = currentLocation
        }
        
        self.tableView.reloadData()
    }
    
    func getDistanceBetweenTwoLocation(location1: PFGeoPoint, location2: PFGeoPoint) -> String {
        var loc1 = CLLocation(latitude: location1.latitude, longitude: location1.longitude)
        var loc2 = CLLocation(latitude: location2.latitude, longitude: location2.longitude)
        
        var distance: Double = Double(loc1.distanceFromLocation(loc2))
        
        var distanceInt: Int = Int(distance)
        if distance < 1000 {
            return "\(distanceInt) m"
            
        }
        return String(format: "%.1f", Double(distanceInt) / 1000) + " km"
    }
}
