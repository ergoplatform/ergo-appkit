#include <stdlib.h>
#include <stdio.h>

#include <libprove.h>

int main(int argc, char **argv) {
  graal_isolate_t *isolate = NULL;
  graal_isolatethread_t *thread = NULL;
  
  if (graal_create_isolate(NULL, &isolate, &thread) != 0) {
    fprintf(stderr, "graal_create_isolate error\n");
    return 1;
  }
  
  char * boxId   = argv[1];
  char result[2048];
  sign(thread, boxId, result, 2048);
  printf("Result: %s\n", result);

  if (graal_detach_thread(thread) != 0) {
    fprintf(stderr, "graal_detach_thread error\n");
    return 1;
  }
  
  return 0;
}
