package logic

import Config
import Session

abstract class Service(val session:Session, val cfg:Config)