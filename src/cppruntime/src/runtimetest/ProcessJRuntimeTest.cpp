#include <ProcessJRuntimeTest.hpp>

int32_t main(void) {

    //std::chrono::system_clock::time_point start;
    //std::chrono::system_clock::time_point  stop;
    //std::chrono::microseconds sc_elapsed;
    //std::chrono::microseconds mc_elapsed;
    //std::chrono::microseconds diff;
    //std::srand(0);

    //start = std::chrono::system_clock::now();
    //ProcessJTest::singlecore_test sc_test;
    //sc_test.run();
    //stop = std::chrono::system_clock::now();
    //sc_elapsed = std::chrono::duration_cast<std::chrono::microseconds>(stop - start);
    //std::cout << "elapsed time of sc_test: "
    //          << sc_elapsed.count()
    //          << " microseconds\n";

    /* TODO: this test doesn't work because the cores still aren't isolated
     * the correct way. it might need to be rewritten such that the threads
     * are even created for the multicore scheduler using pthreads instead
     * of std::thread. or, alternatively, make the threads wait on a
     * condition variable that would allow them to all sync up in a way
     * and wait until all the others have been isolated before starting them
     */
    //start = std::chrono::system_clock::now();
    //ProcessJTest::multicore_test mc_test;
    //mc_test.run();
    //stop = std::chrono::system_clock::now();
    //mc_elapsed = std::chrono::duration_cast<std::chrono::microseconds>(stop - start);
    //std::cout << "elapsed time of mc_test: "
    //          << mc_elapsed.count()
    //          << " microseconds\n";

    //if(mc_elapsed > sc_elapsed)
    //{
    //    std::cout << "mc_test took longer than sc_test\n";
    //    diff = mc_elapsed - sc_elapsed;
    //}
    //else if(mc_elapsed < sc_elapsed)
    //{
    //    std::cout << "sc_test took longer than mc_test\n";
    //    diff = sc_elapsed - mc_elapsed;
    //}
    //else
    //{
    //    std::cout << "somehow both tests were the same length of time\n";
    //    diff = mc_elapsed - sc_elapsed;
    //}

    //std::cout << "total time difference between sc/mc: "
    //          << diff.count() << " microseconds\n";

    //ProcessJTest::one2one_test oto_test;
    //oto_test.run();

    //ProcessJTest::one2many_test otm_test;
    //otm_test.run();

    //ProcessJTest::many2one_test mto_test;
    //mto_test.run();

    //ProcessJTest::many2many_test mtm_test;
    //mtm_test.run();

    //ProcessJTest::timer_test t_test;
    //t_test.run();

    //ProcessJTest::barrier_test b_test;
    //b_test.run();

    //ProcessJTest::alt_test a_test;
    //a_test.run();

    //ProcessJTest::record_test r_test;
    //r_test.run();

    //ProcessJTest::protocol_test pr_test;
    //pr_test.run();

    //ProcessJTest::anonproc_test ap_test;
    //ap_test.run();

    //ProcessJTest::par_test p_test;
    //p_test.run();

    //ProcessJTest::static_method_test sm_test;
    //sm_test.run();

    //ProcessJTest::string_test s_test;
    //s_test.run();
    // Set the output to raw

    // Set the terminal to raw mode and create a terminal window instance
    ProcessJSystem::System::SetRawMode();
    ProcessJSystem::TerminalWindow window(160, 48);

    ProcessJTest::ArrayBatch arrayBatch;

    // Set it as the root view
    window.setRootView(arrayBatch);

    arrayBatch();

    // Set it back to cooked
    ProcessJSystem::System::SetCookedMode();

    return 0;

}
