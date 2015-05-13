//
//  AddFriendViewController.swift
//  MobiTrack
//
//  Created by Buğra Öner on 22.04.2015.
//  Copyright (c) 2015 Buğra Öner. All rights reserved.
//

import UIKit
import MessageUI
import Parse

class AddFriendViewController: UITabBarController, MFMessageComposeViewControllerDelegate, CLLocationManagerDelegate {
    
    var parseManager = ParseManager()
    var userDefaults = NSUserDefaults.standardUserDefaults()
    
    var friendList: [User] = [User]()

    override func viewDidLoad() {
        super.viewDidLoad()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func messageComposeViewController(controller: MFMessageComposeViewController!, didFinishWithResult result: MessageComposeResult) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func addFriendAlert(title: String) {
        var recieverPhoneNoTextField: UITextField?
        let alertController = UIAlertController(title: title, message: "", preferredStyle: .Alert)
        
        alertController.addTextFieldWithConfigurationHandler { (textField) -> Void in
            recieverPhoneNoTextField = textField
            recieverPhoneNoTextField?.placeholder = "Enter Phone Number"
        }
        
        var addFriendAction = UIAlertAction(title: "OK", style: UIAlertActionStyle.Default) { UIAlertAction in
            
            var userPhoneNo = self.userDefaults.objectForKey("userPhoneNo") as! String
            
            if self.parseManager.isUserInParse(recieverPhoneNoTextField!.text!) {
                var (status,user) = self.parseManager.friendshipStatus(userPhoneNo, user2: recieverPhoneNoTextField!.text!)
                if status == 0 {
                    if user == 1 {
                        self.addFriendAlert("You already sent a friendship request")
                    }
                    else {
                        self.addFriendAlert("You already have a friendship request from this user")
                    }
                }
                else if status == 1 {
                    if user == 1 {
                        self.addFriendAlert("You cannot send friendship request to this user")
                    }
                    else {
                        self.parseManager.sendFriendshipRequest(recieverPhoneNoTextField!.text!)
                    }
                }
                else if status == 2 {
                    self.addFriendAlert("Already friend !")
                }
                else {
                    self.parseManager.sendFriendshipRequest(recieverPhoneNoTextField!.text!)
                }
            }
            else {
                self.addFriendAlert("There is no user with this phone number !")
            }
        }
        var cancelAction = UIAlertAction(title: "Cancel", style: UIAlertActionStyle.Cancel) { (action) -> Void in
            
        }
        alertController.addAction(addFriendAction)
        alertController.addAction(cancelAction)
        self.presentViewController(alertController, animated: true, completion: nil)
    }
    
    @IBAction func addFriend(sender: UIBarButtonItem) {
        addFriendAlert("Add Friend")
    }
    
    @IBAction func sendMessage(sender: UIBarButtonItem) {
        var messageVC = MFMessageComposeViewController()
        messageVC.body = "I need your help! Please contact me!"
        
        if userDefaults.objectForKey("emergencyFriends") != nil {
            messageVC.recipients = userDefaults.objectForKey("emergencyFriends") as! [String]
        }
        messageVC.messageComposeDelegate = self
        self.presentViewController(messageVC, animated: false, completion: nil)
    }
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
