//
//  FriendsMapViewController.swift
//  MobiTrack
//
//  Created by Buğra Öner on 22.04.2015.
//  Copyright (c) 2015 Buğra Öner. All rights reserved.
//

import UIKit
import MapKit
import Parse

class FriendsMapViewController: UIViewController, MKMapViewDelegate {
    
    @IBOutlet var friendsMap: MKMapView!
    
    var parseManager: ParseManager = ParseManager()
    var userDefaults = NSUserDefaults.standardUserDefaults()
    var userPhoneNo: String?
    var friendList: [User]?

    override func viewDidLoad() {
        super.viewDidLoad()
        userPhoneNo = userDefaults.objectForKey("userPhoneNo") as? String
        friendList = parseManager.getFriends(userPhoneNo!)
        //showLocations()
        self.zoomToFitMapAnnotations()
    }
    
    func zoomToFitMapAnnotations(){
        
        var topLeftCoord = CLLocationCoordinate2D(latitude: -90, longitude: 180)
        var bottomRightCoord = CLLocationCoordinate2D(latitude: 90, longitude: -180)
        
        if friendList!.isEmpty {
            println("hic arkdas yok")
            return
        }
        
        for friend in self.friendList! {
            topLeftCoord.longitude = fmin(topLeftCoord.longitude, friend.lastLocation!.longitude)
            topLeftCoord.latitude = fmax(topLeftCoord.latitude, friend.lastLocation!.latitude)
            
            bottomRightCoord.longitude = fmax(bottomRightCoord.longitude, friend.lastLocation!.longitude)
            bottomRightCoord.latitude = fmin(bottomRightCoord.latitude, friend.lastLocation!.latitude)
            
            var annotation = MKPointAnnotation()
            annotation.coordinate = CLLocationCoordinate2D(latitude: friend.lastLocation!.latitude, longitude: friend.lastLocation!.longitude)
            
            annotation.title = friend.username
            
            self.friendsMap!.addAnnotation(annotation)
            
            var region: MKCoordinateRegion = MKCoordinateRegion(center: topLeftCoord, span: MKCoordinateSpan(latitudeDelta: 0, longitudeDelta: 0))
            region.center.latitude = topLeftCoord.latitude - (topLeftCoord.latitude - bottomRightCoord.latitude) * 0.5
            region.center.longitude = topLeftCoord.longitude + (bottomRightCoord.longitude - topLeftCoord.longitude) * 0.5
            region.span.latitudeDelta = fabs(topLeftCoord.latitude - bottomRightCoord.latitude) * 2
            region.span.longitudeDelta = fabs(bottomRightCoord.longitude - topLeftCoord.longitude) * 2
            
            self.friendsMap.regionThatFits(region)
            self.friendsMap.setRegion(region, animated: true)
        }
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    func mapView(mapView: MKMapView!, viewForAnnotation annotation: MKAnnotation!) -> MKAnnotationView! {
        let reuseId = "pin"
        
        var pinView = mapView.dequeueReusableAnnotationViewWithIdentifier(reuseId) as? MKPinAnnotationView
        if pinView == nil {
            pinView = MKPinAnnotationView(annotation: annotation, reuseIdentifier: reuseId)
            pinView!.canShowCallout = true
            pinView!.animatesDrop = true
            pinView!.pinColor = .Green
        }
        else {
            pinView!.annotation = annotation
        }
        return pinView
    }
    
    func showLocations() {
        let span = MKCoordinateSpanMake(0.03,0.03)
        let region = MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: 41.168958, longitude: 29.562818), span: span)
        friendsMap.setRegion(region, animated: true)
        
        for friend in friendList! {
            var annotation = MKPointAnnotation()
            annotation.coordinate = CLLocationCoordinate2D(latitude: friend.lastLocation!.latitude, longitude: friend.lastLocation!.longitude)
            annotation.title = friend.username
            friendsMap.addAnnotation(annotation)
        }
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
