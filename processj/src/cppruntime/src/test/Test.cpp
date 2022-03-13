#include <Test.hpp>

int32_t main(void)
{
    // std::chrono::system_clock::time_point start;
    // std::chrono::system_clock::time_point  stop;
    // std::chrono::microseconds sc_elapsed;
    // std::chrono::microseconds mc_elapsed;
    // std::chrono::microseconds diff;
    // std::srand(0);

    // start = std::chrono::system_clock::now();
    // pj_tests::singlecore_test sc_test;
    // sc_test.run();
    // stop = std::chrono::system_clock::now();
    // sc_elapsed = std::chrono::duration_cast<std::chrono::microseconds>(stop - start);
    // std::cout << "elapsed time of sc_test: "
    //           << sc_elapsed.count()
    //           << " microseconds\n";

    /* TODO: this test doesn't work because the cores still aren't isolated
     * the correct way. it might need to be rewritten such that the threads
     * are even created for the multicore scheduler using pthreads instead
     * of std::thread. or, alternatively, make the threads wait on a
     * condition variable that would allow them to all sync up in a way
     * and wait until all the others have been isolated before starting them
     */
    // start = std::chrono::system_clock::now();
    // pj_tests::multicore_test mc_test;
    // mc_test.run();
    // stop = std::chrono::system_clock::now();
    // mc_elapsed = std::chrono::duration_cast<std::chrono::microseconds>(stop - start);
    // std::cout << "elapsed time of mc_test: "
    //           << mc_elapsed.count()
    //           << " microseconds\n";

    // if(mc_elapsed > sc_elapsed)
    // {
    //     std::cout << "mc_test took longer than sc_test\n";
    //     diff = mc_elapsed - sc_elapsed;
    // }
    // else if(mc_elapsed < sc_elapsed)
    // {
    //     std::cout << "sc_test took longer than mc_test\n";
    //     diff = sc_elapsed - mc_elapsed;
    // }
    // else
    // {
    //     std::cout << "somehow both tests were the same length of time\n";
    //     diff = mc_elapsed - sc_elapsed;
    // }

    // std::cout << "total time difference between sc/mc: "
    //           << diff.count() << " microseconds\n";

    // pj_tests::one2one_test oto_test;
    // oto_test.run();

    // pj_tests::one2many_test otm_test;
    // otm_test.run();

    // pj_tests::many2one_test mto_test;
    // mto_test.run();

    // pj_tests::many2many_test mtm_test;
    // mtm_test.run();

    // pj_tests::timer_test t_test;
    // t_test.run();

    // pj_tests::barrier_test b_test;
    // b_test.run();

    // pj_tests::alt_test a_test;
    // a_test.run();

    // pj_tests::record_test r_test;
    // r_test.run();

    ProcessJTest::protocol_test pr_test;
    pr_test.run();

    // pj_tests::anonproc_test ap_test;
    // ap_test.run();

    // pj_tests::par_test p_test;
    // p_test.run();

    // pj_tests::static_method_test sm_test;
    // sm_test.run();

    // pj_tests::array_test ar_test;
    // ar_test.run();

    // pj_tests::string_test s_test;
    // s_test.run();

    return 0;
}
