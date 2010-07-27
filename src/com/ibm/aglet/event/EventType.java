/**
 * 
 */
package com.ibm.aglet.event;

/**
 * The available types of events.
 * This enumeration is not used to limit the types of possible events, but to
 * specify the well known types of events.
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 *
 * Jul 27, 2010
 */
public enum EventType {

    CLONING{					// the aglet is currently being cloned

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	    return "cloning";
	}
	
    },
    
    CLONED{					// the aglet has been cloned

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	    return "cloned";
	}
	
    },
    
    CLONE{					// order to clone an agent

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	    return "clone";
	}
	
    },
    
    
    DISPATCHING{				// aglet is being dispatched

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	    return "dispatching";
	}
	
    },
    
    REVERTING{					// aglet has been reverted

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "reverting";
	}
	
    },
    
    ARRIVAL{					// aglet has just arrived

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	    return "arrival";
	}
	
    },
    
    DEACTIVATING{				// aglet has been deactivated

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	    return "deactivating";
	}
	
    },
    
    ACTIVATION{					// aglet has been activated

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	    return "activation";
	}
	
    },
    
    
    AGLET_STARTED{					// context is started

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "started";
	}
	
    },
    
    CONTEXT_SHUTDOWN{					// context disposed

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	    return "context-shutdown";
	}
	
    },
    
    CONTEXT_STARTED{					// context started

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	    return "context-started";
	}
	
    },
    
    
    AGLET_CREATED{					// aglet created

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-created";
	}
	
    },
    
    AGLET_CLONED{					// aglet cloned

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-cloned";
	}
	
    },
    
    AGLET_DISPOSED{					// aglet disposed

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-disposed";
	}
	
    },
    
    AGLET_DISPATCHED{				// aglet dispatched

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-dispacthed";
	}
	
    },
    
    AGLET_REVERTED{				// aglet reverted

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-reverted";
	}
	
    },
    
    AGLET_ARRIVED{				// aglet arrived

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-arrived";
	}
	
    },
    
    AGLET_DEACTIVATED{				// aglet deactivated

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-deactivated";
	}
	
    },
    
    AGLET_SUSPENDED{				// aglet suspended

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-suspended";
	}
	
    },
    
    AGLET_ACTIVATED{				// aglet activated

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-activated";
	}
	
    },
    
    
    AGLET_RESUMED{				// aglet resumed

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-resumed";
	}
	
    },
    
    AGLET_STATE_CHANGED{				// aglet state has changed

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-state-changed";
	}
	
    },
    
    SHOW_DOCUMENT{					// show document

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "show-document";
	}
	
    },
    
   AGLET_MESSAGE{						// aglet message

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "aglet-message";
	}
	
    }, 
    
    NO_REPONSE{

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
	   return "no-response";
	}
	
    }
}
