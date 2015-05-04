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
    
    var timer: dispatch_source_t!
    
    var parseManager: ParseManager = ParseManager()
    var locationManager = CLLocationManager()
    var userDefaults = NSUserDefaults.standardUserDefaults()
    
    var userPhoneNo: String = String()
    var friendList: [User] = [User]()
    
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
        updateLocation()
        friendList = parseManager.getFriends(userPhoneNo)
    }
    
    func updateLocation() {
        let queue = dispatch_get_main_queue()
        timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, queue)
        dispatch_source_set_timer(timer, DISPATCH_TIME_NOW, 10 * NSEC_PER_SEC, 1 * NSEC_PER_SEC)
        
        let delayTime = dispatch_time(DISPATCH_TIME_NOW, Int64(5 * Double(NSEC_PER_SEC)))
        
        dispatch_source_set_event_handler(self.timer) {
            dispatch_after(delayTime, queue) { () -> Void in
                self.parseManager.updateUserLocation(self.locationOfUser)
                println("GIRDI")
            }
        }
        
        dispatch_resume(timer)
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
        // #warning Potentially incomplete method implementation.
        // Return the number of sections.
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete method implementation.
        // Return the number of rows in the section.
        return friendList.count
    }

    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("MyCell", forIndexPath: indexPath) as! UITableViewCell
        cell.textLabel?.text = friendList[indexPath.row].username

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
        println("location degisti")
        self.locationManager.stopUpdatingLocation()
        
    }
}
