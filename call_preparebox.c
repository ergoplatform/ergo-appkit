#include <stdlib.h>
#include <stdio.h>

#include <libpreparebox.h>

int main(int argc, char **argv) {
  graal_isolate_t *isolate = NULL;
  graal_isolatethread_t *thread = NULL;
  
  if (graal_create_isolate(NULL, &isolate, &thread) != 0) {
    fprintf(stderr, "graal_create_isolate error\n");
    return 1;
  }

  // hash mock transaction to obtain id
  char * mockEncodedTx = "962a2f36c22";
  char mockTxId[100];
  hashBlake2b256(thread, mockEncodedTx, mockTxId, sizeof(mockTxId));

  // get script from cmd args and call transaction creation
  char * ergoScript   = argv[1];
  char result[2048];
  prepareBox(thread, mockTxId, ergoScript, result, 2048);

  // print out serialized result
  printf("Result: %s\n", result);

  if (graal_detach_thread(thread) != 0) {
    fprintf(stderr, "graal_detach_thread error\n");
    return 1;
  }
  
  return 0;
}
