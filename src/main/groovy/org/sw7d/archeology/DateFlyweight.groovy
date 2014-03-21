package org.sw7d.archeology

class DateFlyweight {
    static Map<String, Date> dates = [:].withDefault{ String date_with_dashes_yyyyMMdd ->
      Date.parse("yyyy-MM-dd", date_with_dashes_yyyyMMdd)   
    }
	
}

