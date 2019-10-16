#ifndef __LIBPROVE_H
#define __LIBPROVE_H

#include <graal_isolate.h>


#if defined(__cplusplus)
extern "C" {
#endif

void sign(graal_isolatethread_t*, char*, char*, size_t);

#if defined(__cplusplus)
}
#endif
#endif
