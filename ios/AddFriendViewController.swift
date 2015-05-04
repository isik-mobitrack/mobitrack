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
            if self.parseManager.isUserInParse(recieverPhoneNoTextField!.text!) {
                self.parseManager.sendFriendshipRequest(recieverPhoneNoTextField!.text!)
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
        addFriendAlert("Enter a phone number")
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
