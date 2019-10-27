#ifndef __LIBPREPAREBOX_H
#define __LIBPREPAREBOX_H

#include <graal_isolate_dynamic.h>


#if defined(__cplusplus)
extern "C" {
#endif

typedef void (*aggregateBoxes_fn_t)(graal_isolatethread_t*, char*, char*, size_t);

typedef void (*hashBlake2b256_fn_t)(graal_isolatethread_t*, char*, char*, size_t);

typedef void (*prepareBox_fn_t)(graal_isolatethread_t*, char*, char*, char*, size_t);

#if defined(__cplusplus)
}
#endif
#endif
