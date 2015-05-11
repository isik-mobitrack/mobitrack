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

class UsersLastLocationsViewController: UIViewController {
    
    @IBOutlet var userMap: MKMapView!
    
    var parseManager = ParseManager()
    var user: User?
    var longitude: Double?
    var latitude: Double?

    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.title = user!.username
        //showLocations()
        zoomToFitMapAnnotations()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    func showLocations() {
        let span = MKCoordinateSpanMake(0.3,0.3)
        let region = MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: 41.168958, longitude: 29.562818), span: span)
        userMap.setRegion(region, animated: true)
        var locationsOfUser = parseManager.getLocationsOfUser(user!.phoneNo!)
        for location in locationsOfUser {
            var annotation = MKPointAnnotation()
            annotation.coordinate = CLLocationCoordinate2D(latitude: location.latitude, longitude: location.longitude)
            annotation.title = user!.username
            userMap.addAnnotation(annotation)
        }
        
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
