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
        showLocations()
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
    


    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
