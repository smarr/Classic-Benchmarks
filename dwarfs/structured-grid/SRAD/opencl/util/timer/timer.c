#ifdef __cplusplus
extern "C" {
#endif

//===============================================================================================================================================================================================================200
//	INCLUDE/DEFINE
//===============================================================================================================================================================================================================200

#include <stdlib.h>
#include <sys/time.h>

//===============================================================================================================================================================================================================200
//	INCLUDE HEADER
//===============================================================================================================================================================================================================200

#include "./timer.h"

//===============================================================================================================================================================================================================200
//	TIMER FUNCTION
//===============================================================================================================================================================================================================200

 // Returns the current system time in microseconds
long long get_time() {
	struct timeval tv;
	gettimeofday(&tv, NULL);
	return (tv.tv_sec * 1000000) + tv.tv_usec;
}

//===============================================================================================================================================================================================================200
//	END
//===============================================================================================================================================================================================================200

#ifdef __cplusplus
}
#endif
