package `in`.iot.lab.teacherreview.feature_teacherlist.presentation.stateholder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.iot.lab.teacherreview.feature_teacherlist.data.model.IndividualFacultyData
import `in`.iot.lab.teacherreview.feature_teacherlist.data.model.RatingData
import `in`.iot.lab.teacherreview.feature_teacherlist.data.model.RatingParameterData
import `in`.iot.lab.teacherreview.feature_teacherlist.data.model.ReviewPostData
import `in`.iot.lab.teacherreview.feature_teacherlist.data.repository.Repository
import `in`.iot.lab.teacherreview.feature_teacherlist.presentation.state_action.AddReviewAction
import `in`.iot.lab.teacherreview.feature_teacherlist.utils.AddReviewApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class AddReviewViewModel @Inject constructor(
    private val myRepository: Repository
) : ViewModel() {

    var _userInputMarkingRating = MutableStateFlow(1.0)
    val userInputMarkingRating = _userInputMarkingRating.asStateFlow()

    var _userInputAttendanceRating = MutableStateFlow(1.0)
    val userInputAttendanceRating = _userInputAttendanceRating.asStateFlow()

    var _userInputTeachingRating = MutableStateFlow(1.0)
    val userInputTeachingRating = _userInputTeachingRating.asStateFlow()

    var _userInputOverallReview = MutableStateFlow("")
    val userInputOverallReview = _userInputOverallReview.asStateFlow()

    var _userInputMarkingReview = MutableStateFlow("")
    val userInputMarkingReview = _userInputMarkingReview.asStateFlow()


    var _userInputAttendanceReview = MutableStateFlow("")
    val userInputAttendanceReview = _userInputAttendanceReview.asStateFlow()

    var _userInputTeachingReview = MutableStateFlow("")
    val userInputTeachingReview = _userInputTeachingReview.asStateFlow()

    lateinit var selectedTeacherId: IndividualFacultyData
        private set

    var addReviewApiState: AddReviewApiState by mutableStateOf(AddReviewApiState.Initialized)
        private set

    /**
     * This function updates the user Input Marking Rating variable
     *
     * @param flag
     * If the flag is 1 then it increases otherwise it decreases the variable
     */
    fun updateUserInputMarkingRating(flag: Int) {
        if (flag == 1 && userInputMarkingRating.value < 5)
            _userInputMarkingRating.value++
        if (flag == 0 && userInputMarkingRating.value > 0)
            _userInputMarkingRating.value--
    }

    /**
     * This function updates the user Input Attendance Rating variable
     *
     * @param flag
     * If the flag is 1 then it increases otherwise it decreases the variable
     */
    fun updateUserInputAttendanceRating(flag: Int) {
        if (flag == 1 && _userInputAttendanceRating.value < 5)
            _userInputAttendanceRating.value++
        if (flag == 0 && _userInputAttendanceRating.value > 0)
            _userInputAttendanceRating.value--
    }

    /**
     * This function updates the user Input Teaching Rating variable
     *
     * @param flag
     * If the flag is 1 then it increases otherwise it decreases the variable
     */
    fun updateUserInputTeachingRating(flag: Int) {
        if (flag == 1 && _userInputTeachingRating.value < 5)
            _userInputTeachingRating.value++
        if (flag == 0 && _userInputTeachingRating.value > 0)
            _userInputTeachingRating.value--
    }

    fun updateOverallReview(newValue: String) {
        _userInputOverallReview.value = newValue
    }

    fun updateMarkingReview(newValue: String) {
        _userInputMarkingReview.value = newValue
    }

    fun updateAttendanceReview(newValue: String) {
        _userInputAttendanceReview.value = newValue
    }

    fun updateTeachingReview(newValue: String) {
        _userInputTeachingReview.value = newValue
    }

    fun setTeacherId(teacherId: IndividualFacultyData) {
        selectedTeacherId = teacherId
    }


    // Resets all the values to default
    fun resetToDefault() {
        _userInputTeachingRating.value = 0.0
        _userInputMarkingRating.value = 0.0
        _userInputAttendanceRating.value = 0.0


        _userInputOverallReview.value = ""
        _userInputAttendanceReview.value = ""
        _userInputTeachingReview.value = ""
        _userInputMarkingReview.value = ""

        addReviewApiState = AddReviewApiState.Initialized
    }

    // Reset only the Api State to default
    fun resetApiToInitialize() {
        addReviewApiState = AddReviewApiState.Initialized
    }

    // This function posts the Review Data to the database
    fun postReviewData() {

        // Setting the api state to loading
        addReviewApiState = AddReviewApiState.Loading

        // Checking if the Overall Review is given or not
        if (userInputOverallReview.value.isEmpty()) {
            addReviewApiState =
                AddReviewApiState.Failure("Need to Fill the Overall Rating at least")
            return
        }

        // posting the data to the Database
        viewModelScope.launch {

            // Creating the RatingData model object to be passed to the retrofit for posting
            val ratingData = RatingData(
                teachingRating = RatingParameterData(
                    ratedPoints = _userInputTeachingRating.value,
                    description = _userInputTeachingReview.value
                ),
                markingRating = RatingParameterData(
                    ratedPoints = _userInputMarkingRating.value,
                    description = _userInputMarkingReview.value
                ),
                attendanceRating = RatingParameterData(
                    ratedPoints = _userInputAttendanceRating.value,
                    description = _userInputAttendanceReview.value
                )
            )

            // The Actual post data that will be sent to the Database
            val postData = ReviewPostData(
                review = _userInputOverallReview.value,
                rating = ratingData,
                faculty = selectedTeacherId._id
            )

            // CHanging the State of the Api Accordingly
            addReviewApiState = try {
                myRepository.postReviewData(postData)
            } catch (_: ConnectException) {
                AddReviewApiState.Failure("No Internet Connection")
            }
        }
    }

    fun action(action : AddReviewAction){
        when(action){
            AddReviewAction.PostReviewData -> postReviewData()
            AddReviewAction.ResetApiToInitialize -> resetApiToInitialize()
            AddReviewAction.ResetToDefault -> resetToDefault()
            is AddReviewAction.SetTeacherId -> setTeacherId(action.teacherId)
            is AddReviewAction.UpdateAttendanceReview -> updateAttendanceReview(action.review)
            is AddReviewAction.UpdateMarkingReview -> updateMarkingReview(action.review)
            is AddReviewAction.UpdateOverallReview -> updateOverallReview(action.review)
            is AddReviewAction.UpdateTeachingReview -> updateTeachingReview(action.review)
            is AddReviewAction.UpdateUserInputAttendanceRating -> updateUserInputAttendanceRating(action.flag)
            is AddReviewAction.UpdateUserInputMarkingRating -> updateUserInputMarkingRating(action.flag)
            is AddReviewAction.UpdateUserInputTeachingRating -> updateUserInputTeachingRating(action.flag)
        }
    }

}