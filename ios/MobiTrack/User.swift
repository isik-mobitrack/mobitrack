//
//  User.swift
//  MobiTrack
//
//  Created by Buğra Öner on 17.04.2015.
//  Copyright (c) 2015 Buğra Öner. All rights reserved.
//

import Foundation
import Parse

class User {
    var phoneNo: String?
    var username: String?
    var lastLocation: PFGeoPoint?
    
    init() {
        
    }
    
    init(phoneNo: String, username: String, lastLocation: PFGeoPoint) {
        self.phoneNo = phoneNo
        self.username = username
        self.lastLocation = lastLocation
    }
}
