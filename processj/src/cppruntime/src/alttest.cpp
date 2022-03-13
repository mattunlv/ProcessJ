#include <ProcessJRuntime.hpp>
#include <functional>



/**
 * File generated by the ProcessJ Compiler.
 * Package name 'tests'.
 * Code generation for 'alttest.pj'.
 * Target class 'alttest'.
 *
 * @author ProcessJ Group - University of Nevada, Las Vegas.
 */

static const int NUMBER = 9;

class _proc$writer101171377 : public ProcessJRuntime::pj_process
{

public:

    _proc$writer101171377() = delete;
    _proc$writer101171377(ProcessJRuntime::pj_scheduler* sched,
	ProcessJRuntime::pj_one2one_channel<int32_t>* _pd$out1)
    {
        this->sched = sched;
        this->_pd$out1 = _pd$out1;
    }

    virtual ~_proc$writer101171377() = default;

    virtual void run()
    {
        switch (get_label())
        {
            case 0: goto _proc$writer101171377L0; break;
            case 1: goto _proc$writer101171377L1; break;
        }

        _proc$writer101171377L0:

        _pd$out1->write(this, 42);
        this->set_label(1);
        // yield();
        return;

        _proc$writer101171377L1:
        terminate();
        return;
    }

protected:
    // formal variables
    ProcessJRuntime::pj_one2one_channel<int32_t>* _pd$out1;
private:
    ProcessJRuntime::pj_scheduler* sched;
};


class _proc$writer211171377 : public ProcessJRuntime::pj_process
{

public:

    _proc$writer211171377() = delete;
    _proc$writer211171377(ProcessJRuntime::pj_scheduler* sched,
	ProcessJRuntime::pj_one2one_channel<int32_t>* _pd$out2)
    {
        this->sched = sched;
        this->_pd$out2 = _pd$out2;
    }

    virtual ~_proc$writer211171377() = default;

    virtual void run()
    {
        switch (get_label())
        {
            case 0: goto _proc$writer211171377L0; break;
            case 1: goto _proc$writer211171377L1; break;
        }

        _proc$writer211171377L0:

        _pd$out2->write(this, 43);
        this->set_label(1);
        // yield();
        return;

        _proc$writer211171377L1:
        terminate();
        return;
    }

protected:
    // formal variables
    ProcessJRuntime::pj_one2one_channel<int32_t>* _pd$out2;
private:
    ProcessJRuntime::pj_scheduler* sched;
};


class _proc$reader2$682474708 : public ProcessJRuntime::pj_process
{

public:

    int _ld$v1;
    int _ld$index2;
    ProcessJRuntime::Alternation* _ld$alt3;

    std::vector<bool> boolean_guards;
    std::vector<ProcessJRuntime::AlternationGuardType> object_guards;
    bool alt_ready;
    int selected;

    _proc$reader2$682474708() = delete;
    _proc$reader2$682474708(ProcessJRuntime::pj_scheduler* sched,
	ProcessJRuntime::pj_one2one_channel<int32_t>* _pd$in13,
	ProcessJRuntime::pj_one2one_channel<int32_t>* _pd$in24)
    {
        this->sched = sched;
        this->_pd$in13 = _pd$in13;
        this->_pd$in24 = _pd$in24;
    }

    virtual ~_proc$reader2$682474708() = default;

    virtual void run()
    {
        switch (get_label())
        {
            case 0: goto _proc$reader2$682474708L0; break;
            case 1: goto _proc$reader2$682474708L1; break;
            case 2: goto _proc$reader2$682474708L2; break;
            case 3: goto _proc$reader2$682474708L3; break;
        }

        _proc$reader2$682474708L0:

        _ld$v1 = static_cast<int>(0);
        _ld$alt3 = new ProcessJRuntime::Alternation(2, this);
        boolean_guards = { true, true };
        object_guards = { _pd$in13, ProcessJRuntime::Alternation::SKIP };
        alt_ready = _ld$alt3->set_guards(boolean_guards, object_guards);
        selected = static_cast<int>(0);
        _ld$index2 = static_cast<int>(0);

        if (!alt_ready) {
            std::cout << "RuntimeError: One of the boolean pre-guards must be true!" << std::endl;
            abort();
        }

        this->set_not_ready();
        _ld$index2 = _ld$alt3->enable();
        this->set_label(3);
        // yield();
        return;

        _proc$reader2$682474708L3:
        selected = _ld$alt3->disable(_ld$index2);

        switch(selected)
        {
            case 0:
                // i'm a channel read, and my procName is _proc$reader2$682474708
                if (!_pd$in13->is_ready_to_read(this)) {
                    this->set_label(1);
                    // yield();
                    return;
                }

                _proc$reader2$682474708L1:
                _ld$v1 = _pd$in13->read(this);

                _proc$reader2$682474708L2:
                io::println("Got ", _ld$v1, " from writer 1.");
                break;
            case 1:
                io::println("in skip");
                break;
            default:
                break;
        }
        delete _ld$alt3;
        terminate();
        return;
    }

protected:
    // formal variables
    ProcessJRuntime::pj_one2one_channel<int32_t>* _pd$in13;
    ProcessJRuntime::pj_one2one_channel<int32_t>* _pd$in24;
private:
    ProcessJRuntime::pj_scheduler* sched;
};


class _proc$main31169311 : public ProcessJRuntime::pj_process
{

public:

    ProcessJRuntime::pj_one2one_channel<int32_t>* _ld$c14;
    ProcessJRuntime::pj_one2one_channel<int32_t>* _ld$c25;
    ProcessJRuntime::pj_par* _ld$par$1;

    _proc$main31169311() = delete;
    _proc$main31169311(ProcessJRuntime::pj_scheduler* sched,
	ProcessJRuntime::pj_array<std::string>* _pd$args5)
    {
        this->sched = sched;
        this->_pd$args5 = _pd$args5;
    }

    virtual ~_proc$main31169311() = default;

    virtual void run()
    {
        switch (get_label())
        {
            case 0: goto _proc$main31169311L0; break;
            case 1: goto _proc$main31169311L1; break;
        }

        _proc$main31169311L0:

        _ld$c14 = new ProcessJRuntime::pj_one2one_channel<int32_t>();
        _ld$c25 = new ProcessJRuntime::pj_one2one_channel<int32_t>();
        _ld$par$1 = new ProcessJRuntime::pj_par(2, this);

         // THIS DOES NOT WORK WITH C++ (InvocationProcType)
        class _proc$writer101171377_overload_finalize_0 : public _proc$writer101171377
        {
        public:
            _proc$main31169311* parent;

            _proc$writer101171377_overload_finalize_0(ProcessJRuntime::pj_scheduler* sched, ProcessJRuntime::pj_one2one_channel<int32_t>* out, _proc$main31169311* parent)
            : _proc$writer101171377{sched, out}, parent(parent)
            {
            }

            // my vars are ProcessJRuntime::pj_one2one_channel<int32_t>* out
            virtual void finalize()
            {
                parent->_ld$par$1->decrement();
            }
        };

        this->sched->insert(new _proc$writer101171377_overload_finalize_0(this->sched, _ld$c14, this));

         // THIS DOES NOT WORK WITH C++ (InvocationProcType)
        class _proc$reader2$682474708_overload_finalize_1 : public _proc$reader2$682474708
        {
        public:
            _proc$main31169311* parent;

            _proc$reader2$682474708_overload_finalize_1(ProcessJRuntime::pj_scheduler* sched, ProcessJRuntime::pj_one2one_channel<int32_t>* in1, ProcessJRuntime::pj_one2one_channel<int32_t>* in2, _proc$main31169311* parent)
            : _proc$reader2$682474708{sched, in1, in2}, parent(parent)
            {
            }

            // my vars are ProcessJRuntime::pj_one2one_channel<int32_t>* in1, ProcessJRuntime::pj_one2one_channel<int32_t>* in2
            virtual void finalize()
            {
                parent->_ld$par$1->decrement();
            }
        };

        this->sched->insert(new _proc$reader2$682474708_overload_finalize_1(this->sched, _ld$c14, _ld$c25, this));

        if (_ld$par$1->should_yield()) {
            this->set_label(1);
            return;
        }

        _proc$main31169311L1:
        delete _ld$par$1;
        if (_ld$c14) { delete _ld$c14; }
        if (_ld$c25) { delete _ld$c25; }
        terminate();
        return;
    }

protected:
    // formal variables
    ProcessJRuntime::pj_array<std::string>* _pd$args5;
private:
    ProcessJRuntime::pj_scheduler* sched;
};


int main(int argc, char* argv[])
{
    std::cout << "\n *** CREATING SCHEDULER *** \n\n";
    ProcessJRuntime::pj_scheduler sched;

    std::cout << "\n *** CREATING MAIN PROCESS *** \n\n";

    ProcessJRuntime::pj_array<std::string> args(argc);
    for(int i = 0; i < argc; ++i)
    {
        args[i] = argv[i];
    }

    _proc$main31169311* main_proc = new _proc$main31169311(&sched, &args);

    std::cout << "\n *** SCHEDULING MAIN PROCESS *** \n\n";
    sched.insert(main_proc);

    std::cout << "\n *** STARTING SCHEDULER *** \n\n";
    sched.start();
}
