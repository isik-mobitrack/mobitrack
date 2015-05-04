//
//  Friendship.swift
//  MobiTrack
//
//  Created by Buğra Öner on 17.04.2015.
//  Copyright (c) 2015 Buğra Öner. All rights reserved.
//

import Foundation

class Friendship{
    var sender: User?
    var reciever: User?
    var status: Int
    
    init(sender: User, reciever: User, status: Int) {
        self.sender = sender
        self.reciever = reciever
        self.status = status
    }
}