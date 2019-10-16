#ifndef __LIBPROVE_H
#define __LIBPROVE_H

#include <graal_isolate_dynamic.h>


#if defined(__cplusplus)
extern "C" {
#endif

typedef void (*sign_fn_t)(graal_isolatethread_t*, char*, char*, size_t);

#if defined(__cplusplus)
}
#endif
#endif
