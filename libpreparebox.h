#ifndef __LIBPREPAREBOX_H
#define __LIBPREPAREBOX_H

#include <graal_isolate.h>


#if defined(__cplusplus)
extern "C" {
#endif

void aggregateBoxes(graal_isolatethread_t*, char*, char*, size_t);

void hashBlake2b256(graal_isolatethread_t*, char*, char*, size_t);

void prepareBox(graal_isolatethread_t*, char*, char*, char*, size_t);

#if defined(__cplusplus)
}
#endif
#endif
