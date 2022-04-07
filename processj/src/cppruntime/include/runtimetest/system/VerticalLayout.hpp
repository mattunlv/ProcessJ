/*!
 * \brief Class that represents a Vertical Layout
 *
 * A basic class that represents a terminal window.
 *
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 03/13/2022
 */

#ifndef UNLV_PROCESS_J_SYSTEM_VERTICAL_LAYOUT_HPP
#define UNLV_PROCESS_J_SYSTEM_VERTICAL_LAYOUT_HPP

namespace ProcessJSystem{ class VerticalLayout; }

class ProcessJSystem::VerticalLayout : public ProcessJSystem::WindowComponentGroup {

    /// --------------
    /// Public Members

public:

    /*!
     * Primary Constructor, Initializes the VerticalLayout
     * to its' default state
     *
     * \param listener The ProcessJSystem::VerticalLayoutListener to receive
     * callbacks on ProcessJRuntim::VerticalLayout state mutations
     */

    VerticalLayout(ProcessJSystem::WindowComponent::Listener*);

    /*!
     * Invoked when the ProcessJSystem::WindowComponent should
     * measure itself. Passes the available width and height
     *
     * \param width The available width
     * \param height The available height
     */

    void onMeasure(ProcessJSystem::Integer32, ProcessJSystem::Integer32);

};

#endif
