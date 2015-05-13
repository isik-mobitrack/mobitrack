//
//  EmergencyViewController.swift
//  MobiTrack
//
//  Created by Buğra Öner on 10/05/15.
//  Copyright (c) 2015 Buğra Öner. All rights reserved.
//

import UIKit
import MapKit

class EmergencyViewController: UIViewController, MKMapViewDelegate {
    
    var places = [PlaceType]()
    var currentLocation: CLLocationCoordinate2D?
    
    var isTaskFinished: Bool = false
    
    var session = NSURLSession.sharedSession()
    var error: NSError?

    @IBOutlet var sosMap: MKMapView!
    
    let apiKey = "AIzaSyB5OLl85-kwQpa3M5pXCjVWUBjAprwk6cQ"
    var dynamicURL: String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        currentLocation = CurrentLocation.lastLocation!
        
        self.getPlaces()
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func mapView(mapView: MKMapView!, viewForAnnotation annotation: MKAnnotation!) -> MKAnnotationView! {
        var pinView = mapView.dequeueReusableAnnotationViewWithIdentifier("myPin") as? MKPinAnnotationView
        
        if pinView == nil {
            pinView = MKPinAnnotationView(annotation: annotation, reuseIdentifier: "myPin")
            pinView?.canShowCallout = true
            if annotation.subtitle == "Hospital" {
                pinView?.image = UIImage(named: "hospital.png")
            }
            else if annotation.subtitle == "Police Station" {
                pinView?.image = UIImage(named: "police.png")
            }
        }
        else {
            pinView!.annotation = annotation
        }
        
        return pinView
    }
    
    
    func getPlaces() {
        
        dynamicURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=\(currentLocation!.latitude),\(currentLocation!.longitude)&radius=5000&types=hospital|police&sensor=true&key=\(apiKey)"
        
        var urlString = dynamicURL?.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLFragmentAllowedCharacterSet())
        
        var placeURL = NSURL(string: urlString!)
        
        var task = session.dataTaskWithURL(placeURL!, completionHandler: { (data, response, error) -> Void in
            
            var jsonObject: NSDictionary = NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.AllowFragments, error: &self.error) as! NSDictionary
            
            let datas =  jsonObject.valueForKey("results")! as! NSArray
            
            for dictData : AnyObject in datas{
                let dictEach = dictData as! NSDictionary
                let name = dictEach.valueForKey("name")! as! NSString
                println(name)
                let geometry = dictEach.valueForKey("geometry")! as! NSDictionary
                let locations:AnyObject = geometry.valueForKey("location")!
                let lon = locations.valueForKey("lng")! as! Double
                let lat = locations.valueForKey("lat")! as! Double
                let types = dictEach["types"] as! NSArray
                
                var placeType: PlaceType = PlaceType(name: name as String, type: types as! [String], longitude: lon, latitude: lat)
                self.places.append(placeType)
                self.isTaskFinished = true
            }
            dispatch_async(dispatch_get_main_queue(), { () -> Void in
                self.showSOSPlacesOnMap()
            })
        })
        task.resume()
    }
    
    func showSOSPlacesOnMap() {
        var topLeftCoordinate = CLLocationCoordinate2D(latitude: -90, longitude: 180)
        var bottomRightCoordinate = CLLocationCoordinate2D(latitude: 90, longitude: -180)
        
        if self.places.isEmpty {
            return
        }
        
        for place in places {
            topLeftCoordinate.longitude = fmin(topLeftCoordinate.longitude, place.longitude)
            topLeftCoordinate.latitude = fmax(topLeftCoordinate.latitude, place.latitude)
            
            bottomRightCoordinate.longitude = fmax(bottomRightCoordinate.longitude, place.longitude)
            bottomRightCoordinate.latitude = fmin(bottomRightCoordinate.latitude, place.latitude)
            
            var annotation = MKPointAnnotation()
            annotation.coordinate = CLLocationCoordinate2D(latitude: place.latitude, longitude: place.longitude)
            annotation.title = place.name
            
            if find(place.type, "hospital") != nil {
                annotation.subtitle = "Hospital"
            }
            else {
                annotation.subtitle = "Police Station"
            }
            self.sosMap.addAnnotation(annotation)
            
            var region: MKCoordinateRegion = MKCoordinateRegion(center: topLeftCoordinate, span: MKCoordinateSpan(latitudeDelta: 0, longitudeDelta: 0))
            region.center.latitude = topLeftCoordinate.latitude - (topLeftCoordinate.latitude - bottomRightCoordinate.latitude) * 0.5
            region.center.longitude = topLeftCoordinate.longitude + (bottomRightCoordinate.longitude - topLeftCoordinate.longitude) * 0.5
            region.span.latitudeDelta = fabs(topLeftCoordinate.latitude - bottomRightCoordinate.latitude) * 1.1
            region.span.longitudeDelta = fabs(bottomRightCoordinate.longitude - topLeftCoordinate.longitude) * 1.1
            
            self.sosMap.regionThatFits(region)
            self.sosMap.setRegion(region, animated: true)
            
            
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
