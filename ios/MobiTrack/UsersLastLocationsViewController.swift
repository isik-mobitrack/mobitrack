//
//  UsersLastLocationsViewController.swift
//  MobiTrack
//
//  Created by Buğra Öner on 22.04.2015.
//  Copyright (c) 2015 Buğra Öner. All rights reserved.
//

import UIKit
import Parse
import MapKit

class UsersLastLocationsViewController: UIViewController, UIPopoverControllerDelegate {
    
    @IBOutlet var userMap: MKMapView!
    
    var popUpView: UITextView?
    
    var parseManager = ParseManager()
    var user: User?
    var isFriend: Bool?
    var longitude: Double?
    var latitude: Double?

    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.title = user!.username
        
        if isFriend! {
            zoomToFitMapAnnotations()
        }
        else {
            showPopUp()
        }
    }
    
    func showPopUp() {
        popUpView = UITextView(frame: CGRect(x: 0, y: self.view.center.y-100, width: self.view.bounds.width, height: 40))
        popUpView!.text = "You should accept friendship request to see locations of \(self.user!.username!)"
        popUpView?.editable = false
        popUpView!.textColor = UIColor.darkTextColor()
        popUpView!.backgroundColor = UIColor.redColor()
        popUpView!.alpha = 1.0
        self.view.addSubview(popUpView!)
        var timer = NSTimer.scheduledTimerWithTimeInterval(1, target: self, selector: Selector("hidePopUp"), userInfo: nil, repeats: false)
    }
    
    func hidePopUp() {
        UIView.animateWithDuration(4, delay: 0.0, options: UIViewAnimationOptions.CurveEaseIn, animations: {
            self.popUpView?.alpha = 0.0
            }, completion: nil)
        
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func zoomToFitMapAnnotations(){
        
        var locationsOfUser = parseManager.getLocationsOfUser(user!.phoneNo!)
        
        if locationsOfUser.isEmpty {
            return
        }
        
        var topLeftCoord:CLLocationCoordinate2D = CLLocationCoordinate2D(latitude: -90, longitude: 180)
        
        var bottomRightCoord:CLLocationCoordinate2D = CLLocationCoordinate2D(latitude: 90, longitude: -180)
        
        
        for location in locationsOfUser {
            topLeftCoord.longitude = fmin(topLeftCoord.longitude, location.longitude)
            topLeftCoord.latitude = fmax(topLeftCoord.latitude, location.latitude)
            
            bottomRightCoord.longitude = fmax(bottomRightCoord.longitude, location.longitude)
            bottomRightCoord.latitude = fmin(bottomRightCoord.latitude, location.latitude)
            
            var annotation = MKPointAnnotation()
            annotation.coordinate = CLLocationCoordinate2D(latitude: location.latitude, longitude: location.longitude)
            
            
            
            self.userMap!.addAnnotation(annotation)
        }
        
        var centerLat = (topLeftCoord.latitude + bottomRightCoord.latitude) / 2
        var centerLong = (topLeftCoord.longitude + bottomRightCoord.longitude) / 2
        
        var region:MKCoordinateRegion = MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: centerLat, longitude: centerLong), span:MKCoordinateSpan(latitudeDelta: 0, longitudeDelta: 0))
        region.span.latitudeDelta = fabs(topLeftCoord.latitude - bottomRightCoord.latitude) * 2
        region.span.longitudeDelta = fabs(bottomRightCoord.longitude - topLeftCoord.longitude) * 2
        
        self.userMap!.regionThatFits(region)
        self.userMap!.setRegion(region, animated: true)
        
        
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
