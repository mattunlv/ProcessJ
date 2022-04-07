/*!
 * \brief Class that represents a Window Component Callback receiver
 *
 * A basic class that represents a Window Component Callback receiver.
 *
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 03/13/2022
 */

#ifndef UNLV_PROCESS_J_SYSTEM_WINDOW_COMPONENT_LISTENER_HPP
#define UNLV_PROCESS_J_SYSTEM_WINDOW_COMPONENT_LISTENER_HPP

namespace ProcessJSystem{ class WindowComponentListener; }

class ProcessJSystem::WindowComponentListener {

    /// ---------------
    /// Public Members

public:

    /*!
     * Invoked when the window component is dirty and needs to be drawn.
     *
     * \param component The Component to be drawn as a void pointer
     */

   virtual void OnComponentDirty(void*) = 0;

   /*!
    * Invoked when a child view is requesting to be re-measured
    *
    * \parm component The Component that is requesting to be re-measured
    */

   virtual void RequestLayout(void*) = 0;


};

#endif
